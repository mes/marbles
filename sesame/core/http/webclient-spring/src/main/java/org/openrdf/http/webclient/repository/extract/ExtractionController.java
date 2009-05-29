/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient.repository.extract;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

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
import org.openrdf.http.webclient.properties.RDFFormatPropertyEditor;
import org.openrdf.http.webclient.properties.ResourcePropertyEditor;
import org.openrdf.http.webclient.properties.UriPropertyEditor;
import org.openrdf.http.webclient.properties.ValuePropertyEditor;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.rio.RDFWriterRegistry;
import org.openrdf.rio.Rio;

public class ExtractionController extends SimpleFormController {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) {
		HttpSession session = request.getSession();
		HTTPRepository repo = (HTTPRepository)session.getAttribute(SessionKeys.REPOSITORY_KEY);

		binder.registerCustomEditor(Resource.class, new ResourcePropertyEditor(repo.getValueFactory()));
		binder.registerCustomEditor(URI.class, new UriPropertyEditor(repo.getValueFactory()));
		binder.registerCustomEditor(Value.class, new ValuePropertyEditor(repo.getValueFactory()));

		binder.registerCustomEditor(RDFFormat.class, new RDFFormatPropertyEditor());
	}

	@Override
	public ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command,
			BindException errors)
	{
		HTTPRepository repo = (HTTPRepository)request.getSession().getAttribute(SessionKeys.REPOSITORY_KEY);

		ExtractionSettings settings = (ExtractionSettings)command;
		RDFFormat format = settings.getResultFormat();

		RepositoryConnection conn = null;
		try {
			response.setContentType(format.getDefaultMIMEType());
			String filename = "extract";
			if (format.getDefaultFileExtension() != null) {
				filename += "." + format.getDefaultFileExtension();
			}
			response.setHeader("Content-Disposition", "attachment; filename=" + filename);

			RDFWriter writer = Rio.createWriter(format, response.getOutputStream());

			conn = repo.getConnection();
			conn.exportStatements(settings.getSubject(), settings.getPredicate(), settings.getObject(),
					settings.isIncludeInferred(), writer, settings.getContexts());
		}
		catch (RepositoryException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (RDFHandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			try {
				response.getOutputStream().close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			if (conn != null) {
				try {
					conn.close();
				}
				catch (RepositoryException e) {
					e.printStackTrace();
				}
			}
		}

		return null;
	}

	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request) {
		Map<String, Object> result = new HashMap<String, Object>();

		Map<String, String> resultFormats = new TreeMap<String, String>();

		for (RDFWriterFactory factory : RDFWriterRegistry.getInstance().getAll()) {
			RDFFormat resultFormat = factory.getRDFFormat();
			resultFormats.put(resultFormat.getName(), resultFormat.getName());
		}

		result.put("resultFormats", resultFormats);

		return result;
	}
}
