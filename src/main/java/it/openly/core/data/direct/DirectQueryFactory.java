package it.openly.core.data.direct;

public class DirectQueryFactory implements IDirectQueryFactory<Object> {

	private String interfaceFQN = null;
	private boolean singleton = true;
	
	@Override
	public Object getObject() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<?> getObjectType() {
		try {
			return Class.forName(interfaceFQN);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isSingleton() {
		return singleton;
	}
	
	public void setSingleton(boolean value) {
		singleton = value;
	}

	public void setInterfaceFQN(String value) {
		interfaceFQN = value;
	}
}
