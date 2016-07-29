package com.ir.homework.hw6;

import com.ir.homework.hw6.io.ResultEvaluator;
import static com.ir.homework.hw6.Constants.*;

import java.io.IOException;

public final class ResultsEvaluator {

	public static void main(String[] args) throws IOException {
		ResultEvaluator re = new ResultEvaluator(TRECK_EVAL_PATH, TRECK_EVAL_PARAMS);
		re.runEvaluation(QEVL_PATH, true);

	}

}
