package epmc.jani.interaction.database;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * http://stackoverflow.com/questions/14478870/dynamically-load-the-jdbc-driver
 * http://www.kfu.com/~nsayer/Java/dyn-jdbc.html
 * 
 */
class DriverDelegate implements Driver {
    private Driver driver;
    
    DriverDelegate(Driver driver) {
    	assert driver != null;
    	this.driver = driver;
    }
    
    @Override
    public boolean acceptsURL(String u) throws SQLException {
        return driver.acceptsURL(u);
    }
    
    @Override
    public Connection connect(String u, Properties p) throws SQLException {
        return driver.connect(u, p);
    }

	@Override
	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
		return driver.getPropertyInfo(url, info);
	}

	@Override
	public int getMajorVersion() {
		return driver.getMajorVersion();
	}

	@Override
	public int getMinorVersion() {
		return driver.getMinorVersion();
	}

	@Override
	public boolean jdbcCompliant() {
		return driver.jdbcCompliant();
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return driver.getParentLogger();
	}
}
