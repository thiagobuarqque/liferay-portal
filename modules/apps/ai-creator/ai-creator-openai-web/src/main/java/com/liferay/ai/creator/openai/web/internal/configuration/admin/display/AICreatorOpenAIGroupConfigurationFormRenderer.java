/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.creator.openai.web.internal.configuration.admin.display;

import com.liferay.ai.creator.openai.configuration.AICreatorOpenAIGroupConfiguration;
import com.liferay.ai.creator.openai.configuration.manager.AICreatorOpenAIConfigurationManager;
import com.liferay.ai.creator.openai.web.internal.display.context.AICreatorOpenAIGroupConfigurationDisplayContext;
import com.liferay.configuration.admin.display.ConfigurationFormRenderer;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ParamUtil;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Thiago Buarque
 */
@Component(service = ConfigurationFormRenderer.class)
public class AICreatorOpenAIGroupConfigurationFormRenderer
	implements ConfigurationFormRenderer {

	@Override
	public String getPid() {
		return AICreatorOpenAIGroupConfiguration.class.getName();
	}

	@Override
	public Map<String, Object> getRequestParameters(
		HttpServletRequest httpServletRequest) {

		return HashMapBuilder.<String, Object>put(
			"apiKey", ParamUtil.getString(httpServletRequest, "apiKey")
		).put(
			"enableChatGPTToCreateContent",
			ParamUtil.getBoolean(httpServletRequest, "enableChatGPT")
		).put(
			"enableDALLEToCreateImages",
			ParamUtil.getBoolean(httpServletRequest, "enableDALLE")
		).build();
	}

	@Override
	public void render(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)
		throws IOException {

		try {
			RequestDispatcher requestDispatcher =
				_servletContext.getRequestDispatcher(
					"/configuration/openai_group_configuration.jsp");

			httpServletRequest.setAttribute(
				AICreatorOpenAIGroupConfigurationDisplayContext.class.getName(),
				new AICreatorOpenAIGroupConfigurationDisplayContext(
					_aiCreatorOpenAIConfigurationManager, httpServletRequest));

			requestDispatcher.include(httpServletRequest, httpServletResponse);
		}
		catch (Exception exception) {
			throw new IOException(
				"Unable to render /configuration" +
					"/openai_group_configuration.jsp",
				exception);
		}
	}

	@Reference
	private AICreatorOpenAIConfigurationManager
		_aiCreatorOpenAIConfigurationManager;

	@Reference(
		target = "(osgi.web.symbolicname=com.liferay.ai.creator.openai.web)"
	)
	private ServletContext _servletContext;

}