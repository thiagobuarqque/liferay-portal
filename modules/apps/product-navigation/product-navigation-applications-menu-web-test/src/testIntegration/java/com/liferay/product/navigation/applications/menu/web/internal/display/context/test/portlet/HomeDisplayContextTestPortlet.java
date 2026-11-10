/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.product.navigation.applications.menu.web.internal.display.context.test.portlet;

import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;

import jakarta.portlet.Portlet;

import org.osgi.service.component.annotations.Component;

/**
 * @author Gabriel Prates
 */
@Component(
	property = {
		"jakarta.portlet.display-name=Test",
		"jakarta.portlet.name=" + HomeDisplayContextTestPortlet.PORTLET_NAME,
		"jakarta.portlet.version=4.0"
	},
	service = Portlet.class
)
public class HomeDisplayContextTestPortlet extends MVCPortlet {

	public static final String PORTLET_NAME =
		"com_liferay_product_navigation_applications_menu_web_internal_" +
			"display_context_test_portlet_HomeDisplayContextTestPortlet";

}