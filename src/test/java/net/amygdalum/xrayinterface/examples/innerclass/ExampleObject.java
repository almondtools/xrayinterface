package net.amygdalum.xrayinterface.examples.innerclass;

@SuppressWarnings("unused")
public class ExampleObject {

	private String outerState;
	
	private InnerStatic fieldInnerStatic;
	
	public ExampleObject(String outerState)  {
		this.outerState = outerState;
		this.fieldInnerStatic = new InnerStatic();
		fieldInnerStatic.setState(outerState);
	}
	
	private InnerStatic createInnerStatic()  {
		InnerStatic innerStatic = new InnerStatic();
		innerStatic.setState(outerState);
		return innerStatic;
	}

	private InnerStaticWithoutStandardConstructor createInnerStaticWithoutStandardContructor()  {
		InnerStaticWithoutStandardConstructor innerStatic = new InnerStaticWithoutStandardConstructor(outerState);
		return innerStatic;
	}

	private boolean useInnerStatic(InnerStatic arg, String s)  {
		return arg.state != null;
	}

	private boolean useInnerStatic(InnerStaticWithoutStandardConstructor arg)  {
		return arg.state != null;
	}

	private Inner createInner()  {
		return new Inner();
	}

	private static class InnerStatic {
		
		private boolean booleanState;
		private String state;

		public InnerStatic() {
		}
		
		public void setState(String state) {
			this.state = state;
		}
		
	}
	
	private static class InnerStaticWithoutStandardConstructor {
		private String state;

		public InnerStaticWithoutStandardConstructor(String state) {
			this.state = state;
		}
		
	}
	
	private class Inner {
		
		private String state;

		public Inner() {
			this.state = outerState;
		}
		
	}
}
