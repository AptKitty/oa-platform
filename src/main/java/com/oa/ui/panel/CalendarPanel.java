package com.oa.ui.panel;

import com.oa.schedule.service.ScheduleService;                         //业务逻辑类，增删改查
import com.oa.schedule.entity.CalendarEvent;                            //日程实体类（数据库），对应数据库里 sch_calendar_event 表的一行
import javax.swing.*;                                                   //界面库；JButton（按钮）、JTable（表格）、JOptionPane（弹窗）、JTextField（输入框）、JComboBox（下拉框）
import java.awt.*;                                                      //布局管理系统：引入 AWT 布局和颜色类。BorderLayout（上下左右中布局）、FlowLayout（横着一排布局）、GridLayout（网格布局）、Color（颜色）、Font（字体）
import java.util.List;                                                  //列表类
import javax.swing.table.DefaultTableModel;                             //表格的数据模型类，增删行
public class CalendarPanel extends BasePanel {//创捷一个名为“日历面板”的类，继承父类“BasePanel
    // createTable() → 建表格
    //createToolBar() → 建工具栏
    //showError() / confirm() → 弹窗提示

    private ScheduleService scheduleService = new ScheduleService();//成员变量，“scheduleService”为业务逻辑对象，与数据库互动
    private JTable eventTable;//“eventTable”为界面上的 表格组件

    public CalendarPanel() {
        initUI();
    }//构造方法,调用“initUI()“方法

    @Override
    public String getPanelKey() {
        return "SCHEDULE";
    }//@Override 表示这个方法是从父类 BasePanel 继承来必须实现的。getPanelKey 返回一个英文标识

    @Override
    public String getPanelTitle() {
        return "日程日历";
    }
    //getPanelKey() 返回一个唯一标识，组员1 用这个字符串来注册面板
    //getPanelTitle() 返回显示在菜单栏上的中文名称
    private void initUI() {
        add(createToolBar(this::refresh, this::addEvent, this::exportExcel), BorderLayout.NORTH);
        //点"刷新" → 调用 refresh()
        //点"新增" → 调用 addEvent()
        //点"导出Excel" → 调用 exportExcel()
        // 意思是放在顶部
        //this::refresh 是"我的 refresh 方法"的简写

        String[] columns = {"ID", "标题", "开始时间", "结束时间", "类型"};//定义表格有 5 列
        eventTable = createTable(columns);//createTable() 是 BasePanel 提供的，创建的表格单元格不可编辑。
        add(new JScrollPane(eventTable), BorderLayout.CENTER);//把表格包一层滚动条，放在中间区域。

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));//创建底部按钮面板。FlowLayout(RIGHT) 表示按钮从右往左排列
        JButton editBtn = new JButton("修改");//两个按钮，，“修改”按钮放在右边
        editBtn.addActionListener(e -> editEvent());//绑事件，点删除调 editEvent()
        JButton delBtn = new JButton("删除");//“删除”按钮放在底部
        delBtn.addActionListener(e -> deleteEvent());//绑事件，点删除调 deleteEvent()
        bottomPanel.add(editBtn);//把修改按钮加入底部面板
        bottomPanel.add(delBtn);//把删除按钮加入底部面板
        add(bottomPanel, BorderLayout.SOUTH);//把整个底部面板加到主面板的底部（SOUTH = 南 = 下）
    }//画UI，顶部工具栏 + 中间表格 + 底部两个按钮

    private void refresh() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();//获取此刻的时间
        java.time.LocalDateTime start = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);//算出本月1号的初始时间。比如今天是6月9号，就算出6月1号0点
        //withDayOfMonth(1) → 把日期设为本月 1 号
        //withHour(0) → 小时设为 0
        //withMinute(0) → 分钟设为 0
        //withSecond(0) → 秒设为 0
        java.time.LocalDateTime end = now.plusMonths(1).withDayOfMonth(1).minusDays(1).withHour(23).withMinute(59);//算出"本月 1 号 00:00:00"
        //plusMonths(1) → 跳到下个月（7 月）
        //withDayOfMonth(1) → 下月 1 号（7 月 1 号）
        //minusDays(1) → 减 1 天（6 月 30 号）
        //withHour(23).withMinute(59) → 23:59
        java.util.List<CalendarEvent> events = scheduleService.getUserEvents(
                getCurrentUserId(), start, end);//调用业务层查数据库

        clearTable(eventTable);//调用父类方法，清空表格所有行。先把旧数据清掉，再填新的
        DefaultTableModel model = (DefaultTableModel) eventTable.getModel();
        for (CalendarEvent e : events) {//循环，events 里有多少条就执行多少次。e 是当前这一条日程
            model.addRow(new Object[]{//往表格加一行。addRow 的参数是一个 Object 数组，数组里每个元素对应一列
                    e.getId(), e.getTitle(), e.getStartTime(), e.getEndTime(), e.getEventType()
            });//取这条日程的 5 个属性：ID、标题、开始时间、结束时间、类型
        }
    }

    private void addEvent() {//addEvent 方法——弹窗新增日程
        JTextField titleField = new JTextField(20);//创建标题输入框，宽度 20 个字符
        JTextField descField = new JTextField(20);//创建描述输入框
        JTextField startField = new JTextField("2026-06-10 09:00", 20);//创建开始时间输入框，默认值 "2026-06-10 09:00"
        JTextField endField = new JTextField("2026-06-10 10:00", 20);//创建结束时间输入框，默认值 "2026-06-10 10:00"
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"PERSONAL", "MEETING", "TASK"});//创建类型下拉框，三个选项：个人、会议、任务

        JPanel form = new JPanel(new GridLayout(5, 2, 5, 5));//创建表单面板。GridLayout(5, 2, 5, 5) → 5 行 2 列，格子之间间距 5 像素
        form.add(new JLabel("标题:"));//加一个标签"标题:"，占第 1 行第 1 格
        form.add(titleField);//加标题输入框，占第 1 行第 2 格
        form.add(new JLabel("描述:"));
        form.add(descField);
        form.add(new JLabel("开始时间:"));
        form.add(startField);
        form.add(new JLabel("结束时间:"));
        form.add(endField);
        form.add(new JLabel("类型:"));
        form.add(typeBox);//下拉列表

        int result = JOptionPane.showConfirmDialog(this, form, "新增日程", JOptionPane.OK_CANCEL_OPTION);//弹出确认对话框。this 是父面板，form 是表单内容，"新增日程" 是窗口标题。OK_CANCEL_OPTION 表示有"确定"和"取消"两个按钮。返回值存到 result 里
        if (result != JOptionPane.OK_OPTION) return;//如果用户没点"确定"（点了取消或关了窗口），直接 return 退出，什么都不做

        try {
            CalendarEvent event = new CalendarEvent();//创建一个空的日程实体对象
            event.setUserId(getCurrentUserId());//设置这条日程属于当前登录用户
            event.setTitle(titleField.getText().trim());//取标题输入框的文字，trim() 去掉首尾空格，设入实体
            event.setDescription(descField.getText().trim());//同理设置描述
            event.setStartTime(java.time.LocalDateTime.parse(startField.getText().trim().replace(" ", "T")));//解析开始时间。用户输入 "2026-06-10 09:00"，replace(" ", "T") 把空格换成 T，变成 "2026-06-10T09:00"，然后 LocalDateTime.parse() 转成时间对象。
            event.setEndTime(java.time.LocalDateTime.parse(endField.getText().trim().replace(" ", "T")));//同理解析结束时间
            event.setEventType((String) typeBox.getSelectedItem());//取下拉框选中的值（"PERSONAL"/"MEETING"/"TASK"），(String) 是强制类型转换。

            scheduleService.addEvent(event);//调用业务层，把日程实体写入数据库
            refresh();//刷新表格，新增的那条就显示出来了
        } catch (Exception ex) {//捕获任何异常
            showError("新增失败: " + ex.getMessage());//弹错误对话框，显示具体错误信息
        }
    }

    private void exportExcel() {
        com.oa.common.ExportUtil.exportToExcel(eventTable, "日程日历");//调用工具类的静态方法。把表格内容导出为 日程日历.xlsx 文件。弹一个保存对话框让用户选位置。
    }//导出 Excel 方法

    private void editEvent() {//修改日程方法
        int row = eventTable.getSelectedRow();//获取用户在表格里选中了第几行。没选的话返回 -1
        if (row < 0) { showError("请先选择一条日程"); return; }//如果没选（row = -1），弹提示然后退出

        JTextField titleField = new JTextField((String) eventTable.getValueAt(row, 1), 20);//创建标题输入框，预填当前选中行的第 1 列（标题列）的值。getValueAt(row, 1) 取第 row 行第 1 列
        JTextField descField = new JTextField(20);//创建描述输入框（空的）
        JTextField startField = new JTextField(eventTable.getValueAt(row, 2).toString().replace("T", " "), 20);//预填结束时间
        JTextField endField = new JTextField(eventTable.getValueAt(row, 3).toString().replace("T", " "), 20);//创建下拉框
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"PERSONAL", "MEETING", "TASK"});//创建下拉框
        typeBox.setSelectedItem(eventTable.getValueAt(row, 4));//把下拉框设为当前行第 4 列的值，比如原来是 MEETING 就显示 MEETING

        JPanel form = new JPanel(new GridLayout(5, 2, 5, 5));
        form.add(new JLabel("标题:")); form.add(titleField);
        form.add(new JLabel("描述:")); form.add(descField);
        form.add(new JLabel("开始时间:")); form.add(startField);
        form.add(new JLabel("结束时间:")); form.add(endField);
        form.add(new JLabel("类型:")); form.add(typeBox);//和新增一样，拼表单。唯一区别是输入框里已经有数据

        if (JOptionPane.showConfirmDialog(this, form, "修改日程",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;//弹窗。点取消就退出

        try {//和新增一样，可能出错
            CalendarEvent event = new CalendarEvent();
            event.setId((Long) eventTable.getValueAt(row, 0));//创建实体，关键区别：设置 ID。有 ID 就是修改，没 ID 就是新增。(Long) 转换是因为表格里存的是 Object 类型。
            event.setUserId(getCurrentUserId());
            event.setTitle(titleField.getText().trim());
            event.setDescription(descField.getText().trim());
            event.setStartTime(java.time.LocalDateTime.parse(startField.getText().trim().replace(" ", "T")));
            event.setEndTime(java.time.LocalDateTime.parse(endField.getText().trim().replace(" ", "T")));
            event.setEventType((String) typeBox.getSelectedItem());//跟新增一样，依次设置各字段

            scheduleService.updateEvent(event);//调修改方法，背后执行
            refresh();//刷新
        } catch (Exception ex) {
            showError("修改失败: " + ex.getMessage());
        }
    }//异常处理和结束

    private void deleteEvent() {//删除方法
        int row = eventTable.getSelectedRow();
        if (row < 0) { showError("请先选择一条日程"); return; }//同修改，先检查有没有选中
        if (!confirm("确定要删除这条日程吗？")) return;//if (!confirm("确定要删除这条日程吗？")) return;

        Long id = (Long) eventTable.getValueAt(row, 0);//取出选中行的 ID
        scheduleService.deleteEvent(id);//调删除方法。执行
        refresh();//刷新表格，被删的那条就消失了
    }

    // ===== 测试入口（最终合并时删除） =====
    public static void main(String[] args) {//测试入口。main 方法，可以直接右键运行
        JFrame frame = new JFrame("日程日历");//创建一个窗口（JFrame），标题"日程日历"
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//点关闭按钮就退出程序
        frame.setSize(800, 600);// frame.setSize(800, 600)
        frame.setLocationRelativeTo(null);//窗口屏幕居中。null 表示相对于屏幕居中
        CalendarPanel panel = new CalendarPanel();//创建一个 CalendarPanel 对象。这行会触发 initUI() 画界面
        panel.setCurrentUser(1L, "测试用户");//假装当前登录用户是 ID=1 的"测试用户"
        frame.add(panel);//把面板放进窗口
        frame.setVisible(true);//让窗口显示出来
        panel.refresh();//加载数据
    }
}