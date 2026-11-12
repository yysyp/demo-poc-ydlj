#“project-management-gantt-raid-v9.xlsx” 是一个项目管理Excel文件，其中：
##i1 “Sprint-Release-Plan” Sheet是按照项目Sprint（两周）粒度计划的“Sprint Goals”以及根据这些“Sprint Goals“拆分的“Major Stories”，Sheet的“C”列的第“4“ , ”5“ , ”6“ , ”7”行是行标题。
##i2 "Gantt-Chart" Sheet是甘特图，第5行是行标题，第5行第“F”列为第一个Sprint的时间区间，第5行第“G”列为第二个Sprint的时间区间，之后的列以此类推。第3行的“F”，“G”，“H”，“I”列的单元格颜色用来分别表示事项的“On Track”， “At Risk”，“Missed”，“Completed” 状态。
##i3 “RAID-Log” Sheet是Risk，Assumption，Issue，Dependency跟踪事项，第一行是列标题。
#根据上述信息，获取项目计划，再根据过去一周的会议记录(下载zoom summaries和本地meeting transcripts)，结合分析出项目有哪些风险点，以及计划需要如何调整然后执行如下：
##o1 把分析结果写出成md报告文档，文档名称带日期，内容先完整英文版本然后完整中文版。
##o2 把Excel文件进行备份，备份文件文件名带日期。
##o3 对“Sprint-Release-Plan” Sheet进行更新，并用红色字体标出调整的部分，括号加上简短原因（英文）。
##o4 对“Gantt-Chart"” Sheet的“D”列“Notes”中加上对应项的状态描述（英文），并对状态颜色进行更新。
##o5 对“RAID-Log” Sheet进行更新（英文）。
##o6 确保Excel文件未损坏，可以正常打开。


