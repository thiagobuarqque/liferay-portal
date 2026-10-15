/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.sitemap.web.internal.configuration.admin.display;

import com.liferay.configuration.admin.display.ConfigurationFormRenderer;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.site.configuration.manager.SitemapConfigurationManager;
import com.liferay.site.sitemap.web.internal.display.context.SitemapGroupConfigurationDisplayContext;

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
public class SitemapGroupConfigurationFormRenderer
	implements ConfigurationFormRenderer {

	@Override
	public String getPid() {
		return "com.liferay.site.internal.configuration." +
			"SitemapGroupConfiguration";
	}

	@Override
	public Map<String, Object> getRequestParameters(
		HttpServletRequest httpServletRequest) {

		return HashMapBuilder.<String, Object>put(
			"includeCategories",
			ParamUtil.getBoolean(httpServletRequest, "includeCategories")
		).put(
			"includePages",
			ParamUtil.getBoolean(httpServletRequest, "includePages")
		).put(
			"includeWebContent",
			ParamUtil.getBoolean(httpServletRequest, "includeWebContent")
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
					"/configuration/sitemap_group_configuration.jsp");

			httpServletRequest.setAttribute(
				SitemapGroupConfigurationDisplayContext.class.getName(),
				new SitemapGroupConfigurationDisplayContext(
					_sitemapConfigurationManager,
					(ThemeDisplay)httpServletRequest.getAttribute(
						WebKeys.THEME_DISPLAY)));

			requestDispatcher.include(httpServletRequest, httpServletResponse);
		}
		catch (Exception exception) {
			throw new IOException(
				"Unable to render /configuration" +
					"/sitemap_group_configuration.jsp",
				exception);
		}
	}

	@Reference(target = "(osgi.web.symbolicname=com.liferay.site.sitemap.web)")
	private ServletContext _servletContext;

	@Reference
	private SitemapConfigurationManager _sitemapConfigurationManager;

}