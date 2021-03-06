package com.atos.coderank.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atos.coderank.entities.RoleEntity;
import com.atos.coderank.services.RoleService;

@RestController
@RequestMapping("api/private/roles")
public class RoleController {

	@Autowired
	@Qualifier("roleService")
	private RoleService rs;

	@GetMapping("/{id}")
	public ResponseEntity<RoleEntity> findRole(@PathVariable String id) {
		Long roleId = Long.parseLong(id);
		RoleEntity rm = this.rs.findById(roleId);
		HttpStatus status = HttpStatus.OK;

		if (null == rm)
			status = HttpStatus.NOT_FOUND;

		return new ResponseEntity<>(rm, status);
	}

	@GetMapping("")
	public ResponseEntity<List<RoleEntity>> findAll() {

		List<RoleEntity> rml = this.rs.findAll();

		return new ResponseEntity<>(rml, HttpStatus.OK);
	}

}
