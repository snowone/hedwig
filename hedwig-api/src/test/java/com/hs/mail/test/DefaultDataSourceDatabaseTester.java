package com.hs.mail.test;

import javax.sql.DataSource;

import org.dbunit.DataSourceDatabaseTester;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;

public class DefaultDataSourceDatabaseTester extends DataSourceDatabaseTester {

	private DataSource dataSource;

	public DefaultDataSourceDatabaseTester(DataSource dataSource) {
		super(dataSource);
		this.dataSource = dataSource;
	}

	public IDatabaseConnection getConnection() throws Exception {
		if (getSchema() != null) {
			return new DatabaseConnection(dataSource.getConnection(),
					getSchema());
		} else {
			return new DatabaseConnection(dataSource.getConnection());
		}
	}
	
}
