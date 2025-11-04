package com.gymmate.shared.converter;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.*;
import java.util.Arrays;

/**
 * Hibernate UserType for PostgreSQL text[] arrays.
 * Handles conversion between String[] (Java) and text[] (PostgreSQL).
 */
public class StringArrayType implements UserType<String[]> {

    @Override
    public int getSqlType() {
        return Types.ARRAY;
    }

    @Override
    public Class<String[]> returnedClass() {
        return String[].class;
    }

    @Override
    public boolean equals(String[] x, String[] y) {
        return Arrays.equals(x, y);
    }

    @Override
    public int hashCode(String[] x) {
        return Arrays.hashCode(x);
    }

    @Override
    public String[] nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner) throws SQLException {
        Array array = rs.getArray(position);
        if (array == null || rs.wasNull()) {
            return null;
        }

        Object javaArray = array.getArray();
        if (javaArray instanceof String[]) {
            return (String[]) javaArray;
        } else if (javaArray instanceof Object[]) {
            Object[] objArray = (Object[]) javaArray;
            return Arrays.stream(objArray)
                    .map(Object::toString)
                    .toArray(String[]::new);
        }

        return null;
    }

    @Override
    public void nullSafeSet(PreparedStatement st, String[] value, int index, SharedSessionContractImplementor session) throws SQLException {
        if (value == null) {
            st.setNull(index, Types.ARRAY);
        } else {
            Array array = session.getJdbcConnectionAccess().obtainConnection()
                    .createArrayOf("text", value);
            st.setArray(index, array);
        }
    }

    @Override
    public String[] deepCopy(String[] value) {
        return value == null ? null : Arrays.copyOf(value, value.length);
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(String[] value) {
        return deepCopy(value);
    }

    @Override
    public String[] assemble(Serializable cached, Object owner) {
        return deepCopy((String[]) cached);
    }

    @Override
    public String[] replace(String[] detached, String[] managed, Object owner) {
        return deepCopy(detached);
    }
}

