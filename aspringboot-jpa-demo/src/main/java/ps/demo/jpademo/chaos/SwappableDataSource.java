package ps.demo.jpademo.chaos;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.jdbc.datasource.AbstractDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

public class SwappableDataSource extends AbstractDataSource {
    private final AtomicReference<DataSource> target = new AtomicReference();

    public SwappableDataSource(DataSource initial) {
        this.target.set(initial);
    }

    public DataSource getTarget() {
        return target.get();
    }

    public synchronized void swap(DataSource newTarget) {
        DataSource old = target.getAndSet(newTarget);
        if (old instanceof HikariDataSource) {
            ((HikariDataSource) old).close();
        } else if (old instanceof AutoCloseable) {
            try {
                ((AutoCloseable) old).close();
            } catch (Exception ignored) {
// ignore
            }
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return target.get().getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return target.get().getConnection(username, password);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return target.get().unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return target.get().isWrapperFor(iface);
    }

}