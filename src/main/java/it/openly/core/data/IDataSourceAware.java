package it.openly.core.data;

import javax.sql.DataSource;

public interface IDataSourceAware {
	DataSource getDataSource();
	void setDataSource(DataSource value);
}
