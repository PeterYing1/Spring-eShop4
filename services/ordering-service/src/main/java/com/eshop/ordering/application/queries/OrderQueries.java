package com.eshop.ordering.application.queries;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * JDBC read-model implementation of {@link IOrderQueries}.
 *
 * <p>Uses raw SQL for multi-table projections without loading full JPA entity
 * graphs, matching the Dapper queries in the .NET source.
 */
@Repository
public class OrderQueries implements IOrderQueries {

    private static final Logger log = LoggerFactory.getLogger(OrderQueries.class);

    private final JdbcTemplate jdbc;

    public OrderQueries(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // -------------------------------------------------------------------------
    // IOrderQueries
    // -------------------------------------------------------------------------

    @Override
    public OrderViewModel getOrder(int orderId) {
        log.debug("Querying order id={}", orderId);

        String sql = """
                SELECT o.[Id]                  AS ordernumber,
                       o.OrderDate             AS date,
                       o.Description           AS description,
                       o.Address_City          AS city,
                       o.Address_Country       AS country,
                       o.Address_State         AS state,
                       o.Address_Street        AS street,
                       o.Address_ZipCode       AS zipcode,
                       os.[Name]               AS status,
                       oi.ProductName          AS productname,
                       oi.Units                AS units,
                       oi.UnitPrice            AS unitprice,
                       oi.PictureUrl           AS pictureurl
                FROM ordering.orders o
                LEFT JOIN ordering.orderItems oi ON o.Id = oi.OrderId
                LEFT JOIN ordering.orderstatus os ON o.OrderStatusId = os.Id
                WHERE o.Id = ?
                """;

        List<Object[]> rows = new ArrayList<>();
        try {
            jdbc.query(sql, ps -> ps.setInt(1, orderId), rs -> {
                while (rs.next()) {
                    rows.add(new Object[]{
                            rs.getInt("ordernumber"),
                            rs.getTimestamp("date") != null ? rs.getTimestamp("date").toInstant() : null,
                            rs.getString("description"),
                            rs.getString("city"),
                            rs.getString("country"),
                            rs.getString("state"),
                            rs.getString("street"),
                            rs.getString("zipcode"),
                            rs.getString("status"),
                            rs.getString("productname"),
                            rs.getInt("units"),
                            rs.getBigDecimal("unitprice"),
                            rs.getString("pictureurl")
                    });
                }
            });
        } catch (EmptyResultDataAccessException e) {
            throw new NoSuchElementException("Order not found: " + orderId);
        }

        if (rows.isEmpty()) {
            throw new NoSuchElementException("Order not found: " + orderId);
        }

        return mapToOrderViewModel(rows);
    }

    @Override
    public List<OrderSummary> getOrdersFromUser(UUID userId) {
        log.debug("Querying orders for userId={}", userId);

        String sql = """
                SELECT o.[Id]               AS ordernumber,
                       o.[OrderDate]        AS date,
                       os.[Name]            AS status,
                       SUM(oi.Units * oi.UnitPrice) AS total
                FROM ordering.orders o
                LEFT JOIN ordering.orderItems oi ON o.Id = oi.OrderId
                LEFT JOIN ordering.orderstatus os ON o.OrderStatusId = os.Id
                LEFT JOIN ordering.buyers ob ON o.BuyerId = ob.Id
                WHERE ob.IdentityGuid = ?
                GROUP BY o.[Id], o.[OrderDate], os.[Name]
                ORDER BY o.[Id]
                """;

        return jdbc.query(sql, ps -> ps.setString(1, userId.toString()), (rs, rowNum) -> mapToOrderSummary(rs));
    }

    @Override
    public List<CardType> getCardTypes() {
        log.debug("Querying card types");
        return jdbc.query(
                "SELECT Id, Name FROM ordering.cardtypes",
                (rs, rowNum) -> new CardType(rs.getInt("Id"), rs.getString("Name")));
    }

    // -------------------------------------------------------------------------
    // Mappers
    // -------------------------------------------------------------------------

    private OrderViewModel mapToOrderViewModel(List<Object[]> rows) {
        Object[] first = rows.get(0);

        OrderViewModel view = new OrderViewModel();
        view.setOrdernumber((Integer) first[0]);
        view.setDate(first[1] != null ? (java.time.Instant) first[1] : null);
        view.setDescription((String) first[2]);
        view.setCity((String) first[3]);
        view.setCountry((String) first[4]);
        // state not in view model – keep
        view.setStreet((String) first[6]);
        view.setZipcode((String) first[7]);
        view.setStatus((String) first[8]);

        List<OrderViewModel.OrderItemSummary> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Object[] row : rows) {
            String productName = (String) row[9];
            if (productName == null) {
                continue; // LEFT JOIN may produce a null row when there are no items
            }
            int units = (Integer) row[10];
            BigDecimal unitPrice = (BigDecimal) row[11];
            String pictureUrl = (String) row[12];

            OrderViewModel.OrderItemSummary item = new OrderViewModel.OrderItemSummary();
            item.setProductname(productName);
            item.setUnits(units);
            item.setUnitprice(unitPrice);
            item.setPictureurl(pictureUrl);
            items.add(item);

            if (unitPrice != null) {
                total = total.add(unitPrice.multiply(BigDecimal.valueOf(units)));
            }
        }

        view.setOrderitems(items);
        view.setTotal(total);
        return view;
    }

    private OrderSummary mapToOrderSummary(ResultSet rs) throws SQLException {
        OrderSummary summary = new OrderSummary();
        summary.setOrdernumber(rs.getInt("ordernumber"));
        var ts = rs.getTimestamp("date");
        if (ts != null) {
            summary.setDate(ts.toInstant());
        }
        summary.setStatus(rs.getString("status"));
        summary.setTotal(rs.getBigDecimal("total"));
        return summary;
    }
}
