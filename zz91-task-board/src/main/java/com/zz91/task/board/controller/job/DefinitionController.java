/**
 * 
 */
package com.zz91.task.board.controller.job;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.quartz.CronExpression;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.zz91.task.board.controller.BaseController;
import com.zz91.task.board.dao.JobDefinitionDao;
import com.zz91.task.board.domain.JobDefinition;
import com.zz91.task.board.domain.JobStatus;
import com.zz91.task.board.dto.ExtResult;
import com.zz91.task.board.dto.Pager;
import com.zz91.task.board.service.JobDefinitionService;
import com.zz91.task.board.service.JobStatusService;
import com.zz91.task.board.thread.TaskControlThread;
import com.zz91.task.board.thread.TaskRunThread;
import com.zz91.task.board.util.ClassHelper;
import com.zz91.task.board.util.MvcUpload;
import com.zz91.task.board.util.StacktraceUtil;
import com.zz91.task.common.AbstractIdxTask;
import com.zz91.util.lang.StringUtils;

/**
 * @author yuyh
 * 
 */
@Controller
public class DefinitionController extends BaseController {

	@Resource
	private JobDefinitionService jobDefinitionService;
	@Resource
	private JobStatusService jobStatusService;

	@RequestMapping
	public ModelAndView index() {
		return null;
	}

	@RequestMapping
	public ModelAndView queryDefinition(Map<String, Object> out, Pager<JobDefinition> page) {
		page = jobDefinitionService.pageJobDefinition(null, JobDefinitionService.GROUP, page);
		return printJson(page, out);
	}

	final static String UPLOAD_ROOT = "/usr/data/task";

	@RequestMapping
	public ModelAndView createDefinition(Map<String, Object> out,
			JobDefinition jobDefinition) {
		ExtResult result = new ExtResult();

		do {
			if (StringUtils.isEmpty(jobDefinition.getJobName())) {
				break;
			}

			jobDefinition.setIsInUse(JobDefinitionDao.ISUSE_FALSE);

			Integer id = jobDefinitionService
					.insertJobDefinition(jobDefinition);
			if (id != null && id.intValue() > 0) {
				result.setSuccess(true);
				result.setData(id);
			}

		} while (false);

		return printJson(result, out);
	}

	@RequestMapping
	public ModelAndView uploadjar(HttpServletRequest request,
			Map<String, Object> out) {
		ExtResult result = new ExtResult();
		String uploadedFile = MvcUpload.localUpload(request, UPLOAD_ROOT, null);
		if (StringUtils.isNotEmpty(uploadedFile)) {
			result.setSuccess(true);
			result.setData(uploadedFile);
		}
		return printJson(result, out);
	}

	@RequestMapping
	public ModelAndView queryDefinitionById(Map<String, Object> out, Integer id) {
		JobDefinition definition = jobDefinitionService
				.queryJobDefinitionById(id);
		List<JobDefinition> list = new ArrayList<JobDefinition>();
		list.add(definition);
//		Pager<JobDefinition> page = new Pager<JobDefinition>();
//		page.setRecords(list);
		return printJson(list, out);
	}

	@RequestMapping
	public ModelAndView updateDefinition(Map<String, Object> out,
			JobDefinition definition) {
		ExtResult result = new ExtResult();
		// definition.setJobClasspath(UPLOAD_ROOT);
		Integer impact = jobDefinitionService.updateJobDefinition(definition);
		if (impact != null && impact.intValue() > 0) {
			// jobParameterService.deleteJobParameterByJobId(definition.getId());
			// 判断任务是否启用状态
			result.setSuccess(true);

			if(JobDefinitionService.GROUP.equals(definition.getJobGroup())){
				if (definition.getIsInUse() != null
						&& "1".equals(definition.getIsInUse())) {
					TaskControlThread.addRunTask(definition);
				}
			}
		}
		return printJson(result, out);
	}

	@RequestMapping
	public ModelAndView pauseTask(Map<String, Object> out, Integer id,
			String jobName, String jobGroup) {
		// 发送消息，使任务停止
		// 改变任务定义的状态
		ExtResult result = new ExtResult();
		// schedulerService.stopJob(jobName, jobGroup);
		TaskControlThread.removeRunningTask(jobName);
		Integer i = jobDefinitionService.stopTask(id);
		if (i != null && i.intValue() > 0) {
			result.setSuccess(true);
		}
		return printJson(result, out);
	}

//	@Deprecated
//	@RequestMapping
//	public ModelAndView pauseSimpleTask(Map<String, Object> out, Integer id,
//			String jobName) {
//		// 发送消息，使任务停止
//		// 改变任务定义的状态
//		jobDefinitionService.stopTask(id);
//		ExtResult result = new ExtResult();
//		ZZSchedulerTask task = RunningSimpleTask.holderTask(jobName);
//		if (task != null) {
//			task.stopTask();
//			result.setSuccess(true);
//		}
//		return printJson(result, out);
//	}
	
	@RequestMapping
	public ModelAndView resumeTask(Map<String, Object> out, Integer id) {
		ExtResult result = new ExtResult();
		JobDefinition definition = jobDefinitionService
				.queryJobDefinitionById(id);
		
		if (definition != null && definition.getCron() != null
				&& CronExpression.isValidExpression(definition.getCron())) {
			TaskControlThread.addRunTask(definition);
			jobDefinitionService.startTask(id);
			result.setSuccess(true);
		}
		return printJson(result, out);
	}

//	@Deprecated
//	@RequestMapping
//	public ModelAndView resumeSimpleTask(Map<String, Object> out, Integer id,
//			String cron, String jobName) {
//		ExtResult result = new ExtResult();
//		jobDefinitionService.startTask(id);
//		if (StringUtils.isNumber(cron)) {
//			JobDefinition definition = jobDefinitionService
//					.queryJobDefinitionById(id);
//			try {
//				ZZSchedulerTask task = (ZZSchedulerTask) ClassHelper.load(
//						definition.getJobClasspath(),
//						definition.getJobClassName()).newInstance();
//				task.startTask(Long.valueOf(definition.getCron()));
//				RunningSimpleTask.putTask(definition.getJobName(), task);
//				result.setSuccess(true);
//			} catch (MalformedURLException e) {
//				e.printStackTrace();
//			} catch (InstantiationException e) {
//				e.printStackTrace();
//			} catch (IllegalAccessException e) {
//				e.printStackTrace();
//			} catch (ClassNotFoundException e) {
//				e.printStackTrace();
//			}
//		}
//		return printJson(result, out);
//	}

	@RequestMapping
	public ModelAndView deleteDefinition(Map<String, Object> out, Integer id) {
		ExtResult result = new ExtResult();
		JobDefinition definition = jobDefinitionService
				.queryJobDefinitionById(id);
		TaskControlThread.removeRunningTask(definition.getJobName());

		Integer impact = jobDefinitionService.deleteJobDefinition(id);
		if (impact!=null && impact > 0) {
			result.setSuccess(true);
		}
		return printJson(result, out);
	}

	@RequestMapping
	public ModelAndView queryStatusOfDefinition(Map<String, Object> out,
			String jobName, Pager<JobStatus> page) {
		page = jobStatusService.pageJobStatusByJobName(jobName, page);
		return printJson(page, out);
	}

	// @RequestMapping
	// public ModelAndView retryTask(Map<String, Object> out, String basetime,
	// Integer id){
	// ExtResult result = new ExtResult();
	//		
	// return printJson(result, out);
	// }

	@RequestMapping
	public ModelAndView doTask(Map<String, Object> out, Integer id,
			String jobName, Date start) throws ParseException {
		ExtResult result = new ExtResult();
		do {
			if (start ==null) {
				break;
			}
			JobDefinition definition = null;
			if (id != null && id.intValue() > 0) {
				definition = jobDefinitionService.queryJobDefinitionById(id);
			} else {
				definition = jobDefinitionService
						.queryJobDefinitionByName(jobName);
			}

			if(definition!=null && definition.getCron()!=null && CronExpression.isValidExpression(definition.getCron())){
				TaskRunThread runThread = new TaskRunThread(definition,
						jobDefinitionService, jobStatusService);
				runThread.setTargetDate(start);
				
				TaskControlThread.excute(runThread);
			}
//			else if(definition.getCron()!=null && StringUtils.isNumber(definition.getCron())){
//				try {
//					JobStatus status = new JobStatus();
//					Date start = new Date();
//					status.setJobName(definition.getJobName());
//					status.setGmtBasetime(DateUtil.getDate(startDate, "yyyy-MM-dd HH:mm:ss"));
//					status.setGmtTrigger(start);
//					status.setResult("运行中...");
//					status.setId(jobStatusService.insertJobStatus(status));
//					
//					ZZSimpleTask task=(ZZSimpleTask) ClassHelper.load(
//							definition.getJobClasspath(),
//							definition.getJobClassName()).newInstance();
//					task.doTask(60);
//					Date end = new Date();
//					status.setRuntime(end.getTime() - start.getTime());
//					jobStatusService.updateJobStatusById(status);
//				} catch (MalformedURLException e) {
//					e.printStackTrace();
//				} catch (InstantiationException e) {
//					e.printStackTrace();
//				} catch (IllegalAccessException e) {
//					e.printStackTrace();
//				} catch (ClassNotFoundException e) {
//					e.printStackTrace();
//				}
//			}
			result.setSuccess(true);

		} while (false);

		return printJson(result, out);
	}

	/*************search index task*********************/
	@RequestMapping
	public ModelAndView createIdxJob(HttpServletRequest request, Map<String, Object> out,
			JobDefinition jobDefinition){
		
		if(jobDefinition.getEndTime()==null){
			jobDefinition.setEndTime(new Date());
		}
		
		Integer i = jobDefinitionService.insertJobDefinition(jobDefinition);
		ExtResult result = new ExtResult();
		if(i!=null && i.intValue()>0){
			result.setSuccess(true);
		}
		
		return printJson(result, out);
	}
	
	@RequestMapping
	public ModelAndView pauseIdxTask(HttpServletRequest request, Map<String, Object> out, 
			String jobName, Integer id){
		
		TaskControlThread.LAST_BUILD_TIME_MAP.remove(jobName);
		TaskControlThread.BUILD_TASK_MAP.remove(jobName);
		
		Integer i = jobDefinitionService.stopTask(id);
		ExtResult result = new ExtResult();
		if (i != null && i.intValue() > 0) {
			result.setSuccess(true);
		}
		
		return printJson(result, out);
	}
	
	@RequestMapping
	public ModelAndView resumeIdxTask(HttpServletRequest request, Map<String, Object> out, 
			Integer id){
		JobDefinition jobDefinition = jobDefinitionService
				.queryJobDefinitionById(id);
		
		if (jobDefinition != null) {
			
			try {
				AbstractIdxTask jobInstance = (AbstractIdxTask) ClassHelper.load(
						jobDefinition.getJobClasspath(), jobDefinition.getJobClassName())
						.newInstance();
				jobInstance.setCron(jobDefinition.getCron());
				
				TaskControlThread.LAST_BUILD_TIME_MAP.put(jobDefinition.getJobName(), jobDefinition.getEndTime().getTime());
				TaskControlThread.BUILD_TASK_MAP.put(jobDefinition.getJobName(), jobInstance);
				
				jobDefinitionService.startTask(id);
				
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
		}
		
		ExtResult result = new ExtResult() ;
		result.setSuccess(true);
		
		return printJson(result, out);
	}
	
	@RequestMapping
	public ModelAndView removeIdxTask(HttpServletRequest request, Map<String, Object> out, 
			Integer id, String jobName){
		TaskControlThread.LAST_BUILD_TIME_MAP.remove(jobName);
		TaskControlThread.BUILD_TASK_MAP.remove(jobName);
		
		Integer impact = jobDefinitionService.deleteJobDefinition(id);
		ExtResult result = new ExtResult();
		if(impact !=null && impact>0){
			result.setSuccess(true);
		}
		
		return printJson(result, out);
	}
	
	@RequestMapping
	public ModelAndView idx(HttpServletRequest request, Map<String, Object> out){
		
		return null;
	}
	
	@RequestMapping
	public ModelAndView doIdxTask(HttpServletRequest request, Map<String, Object> out,
			Date start, Date end, String jobName, Integer id){
		
		JobDefinition def=jobDefinitionService.queryJobDefinitionById(id);
		ExtResult result = new ExtResult();

		if(def!=null){
			Date taskStart = new Date();
			JobStatus status = new JobStatus();
			try {
				
				
				status.setJobName(def.getJobName());
				status.setGmtBasetime(start);
				status.setGmtTrigger(start);
				status.setResult("运行中...");
				status.setId(jobStatusService.insertJobStatus(status));
				
				AbstractIdxTask jobInstance = (AbstractIdxTask) ClassHelper.load(
						def.getJobClasspath(), def.getJobClassName()).newInstance();
				jobInstance.idxPost(start.getTime(), end.getTime());
				
				status.setResult("成功执行");
				result.setSuccess(true);
			} catch (MalformedURLException e) {
				status.setResult(e.getMessage());
				status.setErrorMsg(StacktraceUtil.getStackTrace(e));
			} catch (InstantiationException e) {
				status.setResult(e.getMessage());
				status.setErrorMsg(StacktraceUtil.getStackTrace(e));
			} catch (IllegalAccessException e) {
				status.setResult(e.getMessage());
				status.setErrorMsg(StacktraceUtil.getStackTrace(e));
			} catch (ClassNotFoundException e) {
				status.setResult(e.getMessage());
				status.setErrorMsg(StacktraceUtil.getStackTrace(e));
			} catch (Exception e) {
				status.setResult(e.getMessage());
				status.setErrorMsg(StacktraceUtil.getStackTrace(e));
			}
			
			Date taskEnd = new Date();
			status.setRuntime(taskEnd.getTime() - taskStart.getTime());
			
			jobStatusService.updateJobStatusById(status);
			
		}
		
		return printJson(result, out);
	}
	
	@RequestMapping
	public ModelAndView queryIdxDefinition(Map<String, Object> out, Pager<JobDefinition> page) {
		page = jobDefinitionService.pageJobDefinition(null, JobDefinitionService.GROUP_IDX, page);
		return printJson(page, out);
	}
}
