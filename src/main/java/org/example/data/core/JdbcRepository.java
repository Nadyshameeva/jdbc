package org.example.data.core;

import org.example.data.core.annotation.Column;
import org.example.data.core.annotation.Id;
import org.example.data.core.annotation.IgnoreColumn;
import org.example.data.core.annotation.Model;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public abstract class JdbcRepository<T, ID> {
    protected final Class<T> modelClass;
    protected final String tableName;
    protected final Field idColumn;
    protected final String idColumnName;

    protected final Connection connection;

    public JdbcRepository(Connection connection) {
        ParameterizedType type = (ParameterizedType) this.getClass().getGenericSuperclass();
        this.modelClass = (Class<T>) type.getActualTypeArguments()[0];

        Model model = modelClass.getAnnotation(Model.class);

        if (model != null && !model.value().isEmpty())
            tableName = model.value();
        else
            tableName = deriveTableName(modelClass.getSimpleName());

        this.connection = connection;

        this.idColumn = Arrays.stream(modelClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No @Id field found in entity"));

        Column idColumnAnnotation = idColumn.getAnnotation(Column.class);
        this.idColumnName = (idColumnAnnotation != null && !idColumnAnnotation.value().isEmpty())
                ? idColumnAnnotation.value()
                : idColumn.getName();
    }

    private String deriveTableName(String className) {
        StringBuilder tableName = new StringBuilder();

        for (char c : className.toCharArray()) {
            if (Character.isUpperCase(c)) {
                if (!tableName.isEmpty())
                    tableName.append('_');

                tableName.append(Character.toLowerCase(c));
            } else {
                tableName.append(c);
            }
        }
        return tableName.toString();
    }

    private Map<String, String> resolveColumns() {
        Map<String, String> columns = new HashMap<>();

        for (Field field : modelClass.getDeclaredFields()) {
            IgnoreColumn ignore = field.getAnnotation(IgnoreColumn.class);
            if (ignore != null)
                continue;

            Column columnAnnotation = field.getAnnotation(Column.class);
            String columnName = (columnAnnotation != null && !columnAnnotation.value().isEmpty())
                    ? columnAnnotation.value()
                    : field.getName();

            columns.put(field.getName(), columnName);
        }

        return columns;
    }

    public Optional<T> findById(ID id) throws SQLException {
        String sql = "SELECT * FROM " + tableName + " WHERE " + idColumnName + " = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, id);
            ResultSet rs = stmt.executeQuery();

            return rs.next()
                    ? Optional.of(mapRow(rs))
                    : Optional.empty();
        } catch (SQLException | ReflectiveOperationException e) {
            throw new SQLException("Error executing findById", e);
        }
    }

    public List<T> findAll() throws SQLException {
        String sql = "SELECT * FROM " + tableName;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            List<T> results = new ArrayList<>();

            while (rs.next()) {
                results.add(mapRow(rs));
            }

            return results;
        } catch (SQLException | ReflectiveOperationException e) {
            throw new SQLException("Error executing findAll", e);
        }
    }

    public int save(T entity) throws SQLException {
        idColumn.setAccessible(true);
        Object idValue;

        try {
            idValue = idColumn.get(entity);
        } catch (IllegalAccessException e) {
            throw new SQLException("Unable to access @Id field value", e);
        }

        boolean exists = false;

        if (idValue != null) {
            String checkSql = "SELECT COUNT(*) FROM " + tableName + " WHERE " + idColumnName + " = ?";

            try (PreparedStatement stmt = connection.prepareStatement(checkSql)) {
                stmt.setObject(1, idValue);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next())
                        exists = rs.getInt(1) > 0;
                }
            }
        }

        if (exists)
            return update(entity);
        else
            return insert(entity);
    }

    public int insert(T entity) throws SQLException {
        Map<String, String> columns = resolveColumns();
        String columnNames = String.join(", ", columns.values());

        String placeholders = columns
                .values()
                .stream()
                .map(c -> "?").collect(Collectors.joining(", "));

        String sql = "insert into " + tableName + " (" + columnNames + ") values (" + placeholders + ")";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int index = 1;

            for (String fieldName : columns.keySet()) {
                Field field = modelClass.getDeclaredField(fieldName);
                field.setAccessible(true);
                stmt.setObject(index++, field.get(entity));
            }

            return stmt.executeUpdate();
        } catch (SQLException | ReflectiveOperationException e) {
            throw new SQLException("Error executing save", e);
        }
    }

    public int update(T entity) throws SQLException {
        Map<String, String> columns = resolveColumns();

        String setClause = columns.values().stream()
                .filter(s -> !s.equals(idColumnName))
                .map(s -> s + " = ?")
                .collect(Collectors.joining(", "));

        String sql = "UPDATE " + tableName + " SET " + setClause + " WHERE " + idColumnName + " = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int index = 1;

            for (Map.Entry<String, String> entry : columns.entrySet()) {
                if (!entry.getValue().equals(idColumnName)) {
                    Field field = modelClass.getDeclaredField(entry.getKey());
                    field.setAccessible(true);
                    stmt.setObject(index++, field.get(entity));
                }
            }

            idColumn.setAccessible(true);
            Object idValue = idColumn.get(entity);
            stmt.setObject(index, idValue);

            return stmt.executeUpdate();
        } catch (SQLException | ReflectiveOperationException e) {
            throw new SQLException("Error executing update", e);
        }
    }

    public int delete(T entity) throws SQLException {
        String sql = "delete from " + tableName + " where " + idColumnName + " = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            idColumn.setAccessible(true);
            Object idValue = idColumn.get(entity);
            stmt.setObject(1, idValue);

            return stmt.executeUpdate();
        } catch (SQLException | ReflectiveOperationException e) {
            throw new SQLException("Error executing delete", e);
        }
    }

    public int deleteById(ID id) throws SQLException {
        String sql = "delete from " + tableName + " where " + idColumnName + " = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, id);

            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Error executing deleteById", e);
        }
    }

    protected T mapRow(ResultSet rs) throws ReflectiveOperationException, SQLException {
        T instance = modelClass.getDeclaredConstructor().newInstance();
        Map<String, String> columns = resolveColumns();

        for (Map.Entry<String, String> entry : columns.entrySet()) {
            Field field = modelClass.getDeclaredField(entry.getKey());
            field.setAccessible(true);
            field.set(instance, rs.getObject(entry.getValue()));
        }

        return instance;
    }
}
