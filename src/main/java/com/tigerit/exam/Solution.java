package com.tigerit.exam;

import static com.tigerit.exam.IO.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * All of your application logic should be placed inside this class.
 * Remember we will load your application from our custom container.
 * You may add private method inside this class but, make sure your
 * application's execution points start from inside run method.
 * 
 * @author anirudhya.sarker
 */
public class Solution implements Runnable {
    
    private static final String INPUT_SPLITTER = " ";
    
    @Override
    public void run() {
        int counter = 1;
        int testCaseCounter = readLineAsInteger();
        while (testCaseCounter > 0) {
            HashMap<String, List<String>> tableInfo = new HashMap<>();
            HashMap<String, List<List<Integer>>> tableData = new HashMap<>();
            HashMap<Integer, List<String>> tableQuery = new HashMap<>();
            int numberOfQuery = 0;
            int numberOfTable = readLineAsInteger();
            
            for (int i = 0; i < numberOfTable; i++) {
                String tableName = readLine();
                String[] columnAndRow = readLine().trim().split(INPUT_SPLITTER);
                String[] columnNamesFromInput = readLine().trim().split(INPUT_SPLITTER);
                
                int numberOfColumn = Integer.parseInt(columnAndRow[0]);
                int numberOfRow = Integer.parseInt(columnAndRow[1]);
                
                List<String> columnNames = new ArrayList<>();
                for (int j = 0; j < numberOfColumn; j++)
                    columnNames.add(columnNamesFromInput[j]);
                tableInfo.put(tableName, columnNames);
                
                List<List<Integer>> data = new ArrayList<>();
                for (int k = 0; k < numberOfRow; k++) {
                    String[] rowDatas = readLine().trim().split(INPUT_SPLITTER);
                    List<Integer> temp = new ArrayList<>();
                    for (int l = 0; l < numberOfColumn; l++)
                        temp.add(Integer.parseInt(rowDatas[l]));
                    data.add(temp);
                }
                tableData.put(tableName, data);
            }
            
            int temp = readLineAsInteger();
            numberOfQuery = temp;
            while (temp > 0) {
                List<String> queries = new ArrayList<>();
                for (int m = 0; m < 5; m++) {
                    String tempQuery = readLine();
                    if (!tempQuery.isEmpty())
                        queries.add(tempQuery);
                }
                tableQuery.put((numberOfQuery - temp), queries);
                temp--;
            }
            
            // Output
            printLine("Test: " + counter);
            for (int n = 0; n < numberOfQuery; n++) {
                List<String> joinedColumns = getJoinedColumns(n, tableQuery, tableInfo);
                printLine(String.join(INPUT_SPLITTER, joinedColumns));
                List<List<Integer>> result = prepareData(n, tableInfo, tableData, tableQuery);
                for (List<Integer> rows : result) {
                    List<String> resultAsString = new ArrayList<>(rows.size());
                    for (Integer row : rows)
                      resultAsString.add(String.valueOf(row));
                    printLine(String.join(INPUT_SPLITTER, resultAsString));
                }
                printLine("");
            }
            counter++;
            testCaseCounter--;
        }
    }
    
    /**
     * Prepare data according to JOIN logic
     * 
     * @param queryNumber The query number nQ
     * @param tableInfo The table information
     * @param tableData Data inside the table
     * @param tableQuery All the queries
     * @return The JOINed result set
     */
    private List<List<Integer>> prepareData (int queryNumber, HashMap<String, List<String>> tableInfo, HashMap<String, List<List<Integer>>> tableData, HashMap<Integer, List<String>> tableQuery) {
        List<List<Integer>> data = new ArrayList<>();
        HashMap<String, String> tableAliasInfo = new HashMap<>();
        String leftTableAlias = "";
        String righTableAlias = "";
        
        List<String> query = tableQuery.get(queryNumber);
        String[] queryLineTwo = query.get(1).trim().split(INPUT_SPLITTER);
        String[] queryLineThree = query.get(2).trim().split(INPUT_SPLITTER);
        if (queryLineTwo.length == 3 && queryLineThree.length == 3) {
            leftTableAlias = queryLineTwo[2];
            righTableAlias = queryLineThree[2];
            tableAliasInfo.put(leftTableAlias, queryLineTwo[1]);
            tableAliasInfo.put(righTableAlias, queryLineThree[1]);
        } else {
            tableAliasInfo.put(queryLineTwo[1], queryLineTwo[1]);
            tableAliasInfo.put(queryLineThree[1], queryLineThree[1]);
        }
        
        List<String> columns = Arrays.asList(query.get(3).toLowerCase().split("on")[1].trim().split("\\s*=\\s*"));
        String[] leftJoinColumn = splitDot(columns.get(0));
        String[] rightJoinColumn = splitDot(columns.get(1));
        int indexOfLeftTableJoinColumn = tableInfo.get(tableAliasInfo.get(leftJoinColumn[0])).indexOf(leftJoinColumn[1]);
        int indexOfRightTableJoinColumn = tableInfo.get(tableAliasInfo.get(rightJoinColumn[0])).indexOf(rightJoinColumn[1]);
        List<List<Integer>> leftTableData = tableData.get(tableAliasInfo.get(leftJoinColumn[0]));
        List<List<Integer>> rightTableData = tableData.get(tableAliasInfo.get(rightJoinColumn[0]));
        
        for (int o = 0; o < leftTableData.size(); o++) {
            List<Integer> tempData = new ArrayList<>();
            List<Integer> leftTableRow = leftTableData.get(o);
            for (int p = 0; p < rightTableData.size(); p++) {
                List<Integer> rightTableRow = rightTableData.get(p);
                if (leftTableRow.get(indexOfLeftTableJoinColumn).equals(rightTableRow.get(indexOfRightTableJoinColumn))) {
                    tempData.addAll(getFilteredRowData(leftTableRow, tableAliasInfo.get(leftTableAlias), leftTableAlias, queryNumber, tableQuery, tableInfo));
                    tempData.addAll(getFilteredRowData(rightTableRow, tableAliasInfo.get(righTableAlias), righTableAlias, queryNumber, tableQuery, tableInfo));
                    data.add(tempData);
                }
            }
        }
        return data;
    }
    
    /**
     * Filter result set based on the columns selected
     * 
     * @param rowData The data to be filtered
     * @param tableName The table name
     * @param tableAlias The table alias name
     * @param queryNumber The query number nQ
     * @param tableQuery All the queries
     * @param tableInfo The table information
     * @return The filtered result set
     */
    private List<Integer> getFilteredRowData (List<Integer> rowData, String tableName, String tableAlias, int queryNumber, HashMap<Integer, List<String>> tableQuery, HashMap<String, List<String>> tableInfo) {
        // SELECT * where all columns will be displayed, no filter needed
        if (tableQuery.get(queryNumber).get(0).contains("*"))
            return rowData;
        
        // Only the selected columns will be displayed
        List<Integer> filteredRowData = new ArrayList<>();
        List<String> columns = Arrays.asList(tableQuery.get(queryNumber).get(0).toLowerCase().split("select")[1].trim().split("\\s*,\\s*"));
        for (Integer data : rowData) {
            for (String column : columns) {
                String[] columnSplitted = splitDot(column);
                String columnTableAliasName = columnSplitted[0];
                String columnName = columnSplitted[1];
                if (tableAlias.equalsIgnoreCase(columnTableAliasName))
                    if (rowData.indexOf(data) == getIndexOfColumn(tableInfo, tableName, columnName))
                        filteredRowData.add(data);
            }
        }
        return filteredRowData;
    }
    
    /**
     * Get index number of a specific column
     * 
     * @param tableInfo The table information
     * @param tableName The table name
     * @param columnName The column name
     * @return The index number
     */
    private int getIndexOfColumn (HashMap<String, List<String>> tableInfo, String tableName, String columnName) {
        return tableInfo.get(tableName).indexOf(columnName);
    }
    
    /**
     * Get all the column names to be displayed
     * 
     * @param queryNumber The query number nQ
     * @param tableQuery All the queries
     * @param tableInfo The table information
     * @return The list of columns
     */
    private List<String> getJoinedColumns (int queryNumber, HashMap<Integer, List<String>> tableQuery, HashMap<String, List<String>> tableInfo) {
        List<String> columnNames = new ArrayList<>();
        List<String> tableNames = determineJoinTableNames(tableQuery, queryNumber);
        if (tableQuery.get(queryNumber).get(0).contains("*")) {
            for (String tableName : tableNames) {
                columnNames.addAll(tableInfo.get(tableName));
            }
        } else {
            List<String> columns = Arrays.asList(tableQuery.get(queryNumber).get(0).toLowerCase().split("select")[1].trim().split("\\s*,\\s*"));
            for (String column : columns) {
                columnNames.add(splitDot(column)[1]);
            }
        }
        return columnNames;
    }
    
    /**
     * Get the table names on which JOIN will be applied
     * 
     * @param tableQuery All the queries
     * @param queryNumber The query number nQ
     * @return The list of table names
     */
    private List<String> determineJoinTableNames (HashMap<Integer, List<String>> tableQuery, int queryNumber) {
        List<String> tableNames = new ArrayList<>();
        List<String> query = tableQuery.get(queryNumber);
        tableNames.add(query.get(1).split(INPUT_SPLITTER)[1].trim());
        tableNames.add(query.get(2).split(INPUT_SPLITTER)[1].trim());
        return tableNames;
    }
    
    /**
     * Split a string containing dot
     * 
     * @param stringToBeSplitted The string which needs to be split
     * @return The split string
     */
    private String[] splitDot (String stringToBeSplit) {
        return stringToBeSplit.toLowerCase().trim().replace(".", INPUT_SPLITTER).split(INPUT_SPLITTER);
    }
}
