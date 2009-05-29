package org.openrdf.sail.rdbms.algebra;

import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.QueryModelVisitor;
import org.openrdf.query.algebra.helpers.QueryModelTreePrinter;
import org.openrdf.sail.rdbms.algebra.base.RdbmsQueryModelNodeBase;
import org.openrdf.sail.rdbms.algebra.base.RdbmsQueryModelVisitorBase;
import org.openrdf.sail.rdbms.algebra.base.SqlExpr;
import org.openrdf.sail.rdbms.algebra.base.UnarySqlOperator;
import org.openrdf.sail.rdbms.optimizers.SqlConstantOptimizer;

/**
 * SqlMbrContains function 
 * (code largely based on BinarySqlOperator.java)
 * 
 * @author Christian Becker
 */
public class SqlMbrContains extends RdbmsQueryModelNodeBase
		implements SqlExpr {
	private StringValue pointSW, pointNE;
	private SqlExpr pointCol;

	public SqlMbrContains(StringValue pointSW, StringValue pointNE, SqlExpr pointCol) {
		super();
		setPointSW(pointSW);
		setPointNE(pointNE);
		setPointCol(pointCol);
	}
	
	@Override
	public <X extends Exception> void visit(
			RdbmsQueryModelVisitorBase<X> visitor) throws X {
		visitor.meet(this);
	}	

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
			throws X {
		pointSW.visit(visitor);
		pointNE.visit(visitor);
		pointCol.visit(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current,
			QueryModelNode replacement) {
		if (pointSW == current) {
			setPointSW((StringValue) replacement);
		} else if (pointNE == current) {
			setPointNE((StringValue) replacement);
		} else if (pointCol == current) {
			setPointCol((PointColumn) replacement);
		} else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public SqlMbrContains clone() {
		SqlMbrContains clone = (SqlMbrContains) super.clone();
		clone.setPointSW((StringValue) pointSW.clone());
		clone.setPointNE((StringValue) pointNE.clone());
		clone.setPointCol((PointColumn) pointCol.clone());
		return clone;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pointSW == null) ? 0 : pointSW.hashCode());
		result = prime * result + ((pointNE == null) ? 0 : pointNE.hashCode());
		result = prime * result + ((pointCol == null) ? 0 : pointCol.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final SqlMbrContains other = (SqlMbrContains) obj;
		if (pointSW == null) {
			if (other.pointSW != null)
				return false;
		} else if (!pointSW.equals(other.pointSW))
			return false;
		if (pointNE == null) {
			if (other.pointNE != null)
				return false;
		} else if (!pointNE.equals(other.pointNE))
			return false;
		if (pointCol == null) {
			if (other.pointCol != null)
				return false;
		} else if (!pointCol.equals(other.pointCol))
			return false;
		return true;
	}

	@Override
	public String toString() {
		QueryModelTreePrinter treePrinter = new QueryModelTreePrinter();
		SqlMbrContains clone = this.clone();
		UnarySqlOperator parent = new UnarySqlOperator(clone){
			@Override
			public <X extends Exception> void visit(
					RdbmsQueryModelVisitorBase<X> visitor) throws X {
				visitor.meetOther(this);
			}};
		new SqlConstantOptimizer().optimize(clone);
		parent.getArg().visit(treePrinter);
		return treePrinter.getTreeString();
	}

	public StringValue getPointSW() {
		return pointSW;
	}

	public void setPointSW(StringValue pointSW) {
		this.pointSW = pointSW;
		pointSW.setParentNode(this);
	}

	public StringValue getPointNE() {
		return pointNE;
	}

	public void setPointNE(StringValue pointNE) {
		this.pointNE = pointNE;
		pointNE.setParentNode(this);
	}

	public SqlExpr getPointCol() {
		return pointCol;
	}

	public void setPointCol(SqlExpr pointCol) {
		this.pointCol = pointCol;
		pointCol.setParentNode(this);
	}

	/**
	 * Converts the parameters to a MySQL <code>POLYGON</code> expression, such as  
	 * <code>POLYGON((sw_long sw_lat, ne_long sw_lat, ne_long ne_lat, sw_long ne_lat, sw_long sw_lat))</code>
	 */
	public SqlExpr toPolygon() {
		String[] sw = pointSW.getValue().split(" "); /* lat long */
		String[] ne = pointNE.getValue().split(" "); /* lat long */
		return new StringValue("POLYGON((" + sw[1] /* sw_long */ + " " + sw[0] /* sw_lat */ + ", "
										   + ne[1] /* ne_long */ + " " + sw[0] /* sw_lat */ + ", "
										   + ne[1] /* ne_long */ + " " + ne[0] /* ne_lat */ + ", "
										   + sw[1] /* sw_long */ + " " + ne[0] /* ne_lat */ + ", "
										   + sw[1] /* sw_long */ + " " + sw[0] /* sw_lat */ + "))");
	}
}
