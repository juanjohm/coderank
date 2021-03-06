package com.atos.coderank.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atos.coderank.components.JsonSerializers;
import com.atos.coderank.entities.BadgesEntity;
import com.atos.coderank.entities.GroupEntity;
import com.atos.coderank.entities.ProjectEntity;
import com.atos.coderank.services.BadgesService;
import com.atos.coderank.services.GroupService;
import com.atos.coderank.services.ProjectMetricsService;
import com.atos.coderank.services.ProjectService;
import com.atos.coderank.services.RankingService;

@RestController
@RequestMapping("api/private/projects")
public class ProjectController {

	@Autowired
	@Qualifier("badgesService")
	private BadgesService bs;
	
	@Autowired
	@Qualifier("projectMetricsService")
	private ProjectMetricsService pms;
	
	@Autowired
	@Qualifier("rankingService")
	private RankingService rs;
	
	@Autowired
	@Qualifier("projectService")
	private ProjectService ps;

	@Autowired
	@Qualifier("groupService")
	private GroupService gs;

	@Autowired
	@Qualifier("jsonSerializers")
	private JsonSerializers js;

	@GetMapping("")
	public ResponseEntity<List<ProjectEntity>> findAll(HttpServletRequest req) {
		String groupsId = req.getParameter("groupsid");
		List<ProjectEntity> list;

		if (groupsId != null) {
			String[] groupsIds = groupsId.split(",");
			List<Long> groupsIdsL = new ArrayList<>();

			for (String id : groupsIds)
				groupsIdsL.add(Long.parseLong(id));

			list = this.ps.findAllByGroupId(groupsIdsL);

		} else {
			list = this.ps.findAll();
		}

		for (int i = 0; i < list.size(); i++) {
			list.get(i).setSerializedGroup(this.js.groupEntitySerializer(list.get(i)));
			list.get(i).setSerializedBadges(this.js.badgeListSerializer(list.get(i)));
		}

		return new ResponseEntity<>(list, HttpStatus.OK);
	}

	@GetMapping("/{projectid}")
	public ResponseEntity<ProjectEntity> findById(@PathVariable String projectid) {
		ProjectEntity project = this.ps.findById(projectid);
		HttpStatus status = HttpStatus.OK;

		if (project == null)
			status = HttpStatus.NOT_FOUND;
		else {
			project.setSerializedGroup(this.js.groupEntitySerializer(project));
			project.setSerializedBadges(this.js.badgeListSerializer(project));
		}

		return new ResponseEntity<>(project, status);
	}

	@PostMapping("/save")
	public ResponseEntity<ProjectEntity> saveOrUpdate(@RequestBody ProjectEntity project) {
		ProjectEntity returnedProject = this.ps.saveOrUpdate(project);
		HttpStatus status = HttpStatus.OK;

		if (returnedProject == null)
			status = HttpStatus.CONFLICT;
		else  {
			project.setSerializedGroup(this.js.groupEntitySerializer(project));
			project.setSerializedBadges(this.js.badgeListSerializer(project));
		}
		
		return new ResponseEntity<>(returnedProject, status);
	}

	@DeleteMapping("/delete")
	public ResponseEntity<String> delete(@RequestBody ProjectEntity project) {
		String response = "The project with id '" + project.getProjectId() + "' has been deleted.";
		HttpStatus status = HttpStatus.OK;
		ProjectEntity projectToDelete = this.ps.findById(project.getProjectId());

		if (null == projectToDelete) {
			response = "Cannot delete the project with id '" + project.getProjectId() + "'. It doesnt exist.";
			status = HttpStatus.NOT_FOUND;
		} else {
			//Unbind from group
			GroupEntity group = projectToDelete.getGroup();
			if(group != null) {
				group.setProject(null);				
				this.gs.saveOrUpdate(group);
			}
			//Unbind from badges
			List<BadgesEntity> badges = projectToDelete.getBadges();
			for (BadgesEntity badge : badges) {
				badge.removeProject(projectToDelete);
				this.bs.saveOrUpdate(badge);
			}
			//Unbind from ranking
			this.rs.deleteByProjectId(projectToDelete);
			
			//Delete metrics
			this.pms.deleteMetricsByProjectId(projectToDelete);
			
			this.ps.delete(projectToDelete);
		}

		return new ResponseEntity<>(response, status);
	}

}
