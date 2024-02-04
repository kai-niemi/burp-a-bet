package io.burpabet.common.util;

import org.springframework.shell.table.BorderStyle;
import org.springframework.shell.table.TableBuilder;
import org.springframework.shell.table.TableModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class TableUtils {
    private TableUtils() {
    }

    public static String prettyPrint(Map<String, Object> map) {
        List<List<Object>> data = new ArrayList<>();

        List<Object> headers = new ArrayList<>();
        headers.add("key");
        headers.add("value");

        data.add(headers);

        map.forEach((key, value) -> {
            List<Object> row = new ArrayList<>();
            row.add(key);
            row.add(value);
            data.add(row);
        });

        return prettyPrint(new TableModel() {
            @Override
            public int getRowCount() {
                return data.size();
            }

            @Override
            public int getColumnCount() {
                return 2;
            }

            @Override
            public Object getValue(int row, int column) {
                return data.get(row).get(column);
            }
        });
    }

    public static String prettyPrint(TableModel model) {
        TableBuilder tableBuilder = new TableBuilder(model);
        tableBuilder.addInnerBorder(BorderStyle.fancy_light);
        tableBuilder.addHeaderBorder(BorderStyle.fancy_double);
        return tableBuilder.build().render(120);
    }
}
