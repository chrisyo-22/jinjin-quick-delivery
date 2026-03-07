package com.jinjin.service.impl;

import com.jinjin.dto.GoodsSalesDTO;
import com.jinjin.entity.Orders;
import com.jinjin.mapper.OrderMapper;
import com.jinjin.mapper.UserMapper;
import com.jinjin.service.ReportService;
import com.jinjin.service.WorkspaceService;
import com.jinjin.vo.*;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;

    @Override
    public TurnoverReportVO getTurnover(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = buildDateRange(begin, end);
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            Map<String, Object> map = new HashMap<>();
            map.put("status", Orders.COMPLETED);
            map.put("begin", LocalDateTime.of(date, LocalTime.MIN));
            map.put("end", LocalDateTime.of(date, LocalTime.MAX));
            Double turnover = orderMapper.sumByMap(map);
            turnoverList.add(turnover == null ? 0.0 : turnover);
        }
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = buildDateRange(begin, end);
        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Integer newUser = getUserCount(beginTime, endTime);
            Integer totalUser = getUserCount(null, endTime);
            newUserList.add(newUser);
            totalUserList.add(totalUser);
        }

        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .build();
    }

    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = buildDateRange(begin, end);
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Integer orderCount = getOrderCount(beginTime, endTime, null);
            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);
            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);
        }

        Integer totalOrderCount = orderCountList.stream().reduce(0, Integer::sum);
        Integer validOrderCount = validOrderCountList.stream().reduce(0, Integer::sum);
        Double orderCompletionRate = totalOrderCount == 0 ? 0.0 : validOrderCount.doubleValue() / totalOrderCount;

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        List<GoodsSalesDTO> goodsSalesDTOList = orderMapper.getSalesTop10(
                LocalDateTime.of(begin, LocalTime.MIN),
                LocalDateTime.of(end, LocalTime.MAX));
        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(goodsSalesDTOList.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList()), ","))
                .numberList(StringUtils.join(goodsSalesDTOList.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList()), ","))
                .build();
    }

    @Override
    public void exportBusinessData(HttpServletResponse response) {
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=business-report.xlsx");

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ServletOutputStream outputStream = response.getOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Business Report");

            int rowIndex = 0;
            XSSFRow titleRow = sheet.createRow(rowIndex++);
            titleRow.createCell(0).setCellValue("Business Report");
            titleRow.createCell(1).setCellValue(begin + " to " + end);

            BusinessDataVO summary = workspaceService.getBusinessData(LocalDateTime.of(begin, LocalTime.MIN), LocalDateTime.of(end, LocalTime.MAX));
            XSSFRow summaryHeader = sheet.createRow(rowIndex++);
            summaryHeader.createCell(0).setCellValue("Turnover");
            summaryHeader.createCell(1).setCellValue("Valid Orders");
            summaryHeader.createCell(2).setCellValue("Completion Rate");
            summaryHeader.createCell(3).setCellValue("Unit Price");
            summaryHeader.createCell(4).setCellValue("New Users");

            XSSFRow summaryRow = sheet.createRow(rowIndex++);
            summaryRow.createCell(0).setCellValue(summary.getTurnover());
            summaryRow.createCell(1).setCellValue(summary.getValidOrderCount());
            summaryRow.createCell(2).setCellValue(summary.getOrderCompletionRate());
            summaryRow.createCell(3).setCellValue(summary.getUnitPrice());
            summaryRow.createCell(4).setCellValue(summary.getNewUsers());

            rowIndex++;
            XSSFRow detailHeader = sheet.createRow(rowIndex++);
            detailHeader.createCell(0).setCellValue("Date");
            detailHeader.createCell(1).setCellValue("Turnover");
            detailHeader.createCell(2).setCellValue("Valid Orders");
            detailHeader.createCell(3).setCellValue("Completion Rate");
            detailHeader.createCell(4).setCellValue("Unit Price");
            detailHeader.createCell(5).setCellValue("New Users");

            for (int i = 0; i < 30; i++) {
                LocalDate current = begin.plusDays(i);
                BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(current, LocalTime.MIN), LocalDateTime.of(current, LocalTime.MAX));
                XSSFRow row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(current.toString());
                row.createCell(1).setCellValue(businessData.getTurnover());
                row.createCell(2).setCellValue(businessData.getValidOrderCount());
                row.createCell(3).setCellValue(businessData.getOrderCompletionRate());
                row.createCell(4).setCellValue(businessData.getUnitPrice());
                row.createCell(5).setCellValue(businessData.getNewUsers());
            }

            for (int i = 0; i < 6; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            outputStream.flush();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to export business data", ex);
        }
    }

    private List<LocalDate> buildDateRange(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        LocalDate current = begin;
        dateList.add(current);
        while (!current.equals(end)) {
            current = current.plusDays(1);
            dateList.add(current);
        }
        return dateList;
    }

    private Integer getUserCount(LocalDateTime beginTime, LocalDateTime endTime) {
        Map<String, Object> map = new HashMap<>();
        map.put("begin", beginTime);
        map.put("end", endTime);
        return userMapper.countByMap(map);
    }

    private Integer getOrderCount(LocalDateTime beginTime, LocalDateTime endTime, Integer status) {
        Map<String, Object> map = new HashMap<>();
        map.put("begin", beginTime);
        map.put("end", endTime);
        map.put("status", status);
        return orderMapper.countByMap(map);
    }
}
