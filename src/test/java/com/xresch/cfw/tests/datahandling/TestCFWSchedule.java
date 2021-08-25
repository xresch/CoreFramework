package com.xresch.cfw.tests.datahandling;

import java.text.ParseException;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.quartz.CronExpression;

import com.xresch.cfw._main.CFW;

public class TestCFWSchedule {

	@Test
	public void testCronExpression() throws ParseException {
		final String expression = "0 0 22 ? * MON-FRI";
		final CronExpression cronExpression = new CronExpression(expression);

		final Date nextValidDate1 = cronExpression.getNextValidTimeAfter(new Date());
		System.out.println(nextValidDate1);

		final Date nextValidDate2 = cronExpression.getNextValidTimeAfter(nextValidDate1);
		System.out.println(nextValidDate2);

		final Date nextValidDate3 = cronExpression.getNextValidTimeAfter(nextValidDate2);
		System.out.println(nextValidDate3);

		final Date nextValidDate4 = cronExpression.getNextValidTimeAfter(nextValidDate3);
		System.out.println(nextValidDate4);

	}
}
