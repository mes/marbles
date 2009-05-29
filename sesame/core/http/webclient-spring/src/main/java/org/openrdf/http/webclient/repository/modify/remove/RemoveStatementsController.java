/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient.repository.modify.remove;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import org.openrdf.http.webclient.SessionKeys;
import org.openrdf.http.webclient.properties.ResourcePropertyEditor;
import org.openrdf.http.webclient.properties.UriPropertyEditor;
import org.openrdf.http.webclient.properties.ValuePropertyEditor;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;

/**
 * @author Herko ter Horst
 */
public class RemoveStatementsController extends SimpleFormController {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) {
		HttpSession session = request.getSession();
		HTTPRepository repo = (HTTPRepository)session.getAttribute(SessionKeys.REPOSITORY_KEY);

		binder.registerCustomEditor(Resource.class, new ResourcePropertyEditor(repo.getValueFactory()));
		binder.registerCustomEditor(URI.class, new UriPropertyEditor(repo.getValueFactory()));
		binder.registerCustomEditor(Value.class, new ValuePropertyEditor(repo.getValueFactory()));
	}

	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command,
			BindException errors)
	{
		logger.info("Removing statements...");
		String actionResult = "repository.modify.remove.statements.success";

		RemovalSpecification toRemove = (RemovalSpecification)command;

		HTTPRepository repo = (HTTPRepository)request.getSession().getAttribute(SessionKeys.REPOSITORY_KEY);
		RepositoryConnection conn = null;
		try {
			conn = repo.getConnection();
			conn.remove(toRemove.getSubject(), toRemove.getPredicate(), toRemove.getObject(),
					toRemove.getContexts());
			conn.commit();
			logger.info("Remove committed.");
		}
		catch (RepositoryException e) {
			logger.warn("Unable to clear repository", e);
			actionResult = "repository.modify.remove.statements.failure";
		}
		finally {
			if (conn != null) {
				try {
					conn.close();
				}
				catch (RepositoryException e) {
					e.printStackTrace();
				}
			}
		}

		return new ModelAndView(getSuccessView(), "actionResult", actionResult);
	}
}
