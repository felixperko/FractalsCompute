package de.felixperko.expressions;

import java.util.List;
import java.util.Set;

public abstract class AbstractExpression implements FractalsExpression {

	public AbstractExpression() {
		super();
	}

	protected StaticSubExpression extractSubExpressionOrPropagate(List<FractalsExpression> staticSubExpressions, Set<String> iterateVarNames, FractalsExpression expr) {
		StaticSubExpression extractedSubExpression = null;
		if (expr.isStatic(iterateVarNames)) {
			extractedSubExpression = new StaticSubExpression(expr);
			staticSubExpressions.add(extractedSubExpression);
		} else {
			expr.extractStaticExpressions(staticSubExpressions, iterateVarNames);
		}
		return extractedSubExpression;
	}

}