package it.openly.core.data;

import javax.sql.DataSource;

public interface IDataSourceAware {
	public DataSource getDataSource();
	public void setDataSource(DataSource value);
}
