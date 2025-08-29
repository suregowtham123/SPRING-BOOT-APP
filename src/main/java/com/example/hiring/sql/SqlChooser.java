package com.example.hiring.sql;

import org.springframework.stereotype.Component;

@Component
public class SqlChooser {

    private static final String QUESTION_1_SQL = """SELECT
  e.emp_id,
  e.first_name,
  e.last_name,
  d.department_name,
  -- Placeholder for Question 1 (ODD) if needed
  0 AS some_field
FROM employee e
JOIN department d ON d.department_id = e.department
ORDER BY e.emp_id DESC;
""";

    private static final String QUESTION_2_SQL = """SELECT
  e.emp_id,
  e.first_name,
  e.last_name,
  d.department_name,
  (
    SELECT COUNT(*)
    FROM employee e2
    WHERE e2.department = e.department
      AND e2.dob > e.dob
  ) AS younger_employees_count
FROM employee e
JOIN department d
  ON d.department_id = e.department
ORDER BY e.emp_id DESC;
""";

    public String chooseForRegNo(String regNo) {
        // parse last two digits
        int n = extractLastTwoDigits(regNo);
        boolean even = (n % 2 == 0);
        return even ? QUESTION_2_SQL : QUESTION_1_SQL;
    }

    private int extractLastTwoDigits(String regNo) {
        String digits = regNo.replaceAll("[^0-9]", "");
        if (digits.length() >= 2) {
            String last2 = digits.substring(digits.length() - 2);
            return Integer.parseInt(last2);
        } else if (digits.length() == 1) {
            return Integer.parseInt(digits);
        } else {
            return 0;
        }
    }
}
