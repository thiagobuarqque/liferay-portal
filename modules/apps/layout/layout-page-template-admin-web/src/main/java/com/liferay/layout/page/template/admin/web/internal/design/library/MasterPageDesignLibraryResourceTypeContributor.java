/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.page.template.admin.web.internal.design.library;

import com.liferay.depot.model.DepotEntry;
import com.liferay.design.library.resource.type.DesignLibraryResourceTypeContributor;
import com.liferay.frontend.data.set.model.FDSActionDropdownItem;
import com.liferay.layout.page.template.admin.constants.LayoutPageTemplateAdminPortletKeys;
import com.liferay.layout.page.template.constants.LayoutPageTemplateActionKeys;
import com.liferay.layout.page.template.constants.LayoutPageTemplateConstants;
import com.liferay.layout.page.template.constants.LayoutPageTemplateEntryTypeConstants;
import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.resource.PortletResourcePermission;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.PortalUtil;

import jakarta.portlet.PortletRequest;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Contributes master pages to the Design Library Admin.
 *
 * <p>
 * This is the first contributor for a type that shares its entry class name
 * with others, so it is the one that exercises {@link #getTypeFilters}.
 * Display page templates, content page templates, and widget page templates
 * are the same {@link LayoutPageTemplateEntry} and are told apart the same
 * way.
 * </p>
 *
 * @author Thiago Buarque
 */
@Component(
	property = "service.ranking:Integer=50",
	service = DesignLibraryResourceTypeContributor.class
)
public class MasterPageDesignLibraryResourceTypeContributor
	implements DesignLibraryResourceTypeContributor {

	@Override
	public String getColor() {
		return "--blue";
	}

	@Override
	public String getCreationItemsModule() {
		return null;
	}

	@Override
	public Map<String, Object> getCreationItemsProps(
		HttpServletRequest httpServletRequest, DepotEntry depotEntry,
		String backURL) {

		return null;
	}

	@Override
	public String getDefaultActionId() {
		return "view";
	}

	@Override
	public String getEntryClassName() {
		return LayoutPageTemplateEntry.class.getName();
	}

	@Override
	public List<FDSActionDropdownItem> getFDSActionDropdownItems(
			HttpServletRequest httpServletRequest, DepotEntry depotEntry,
			String backURL)
		throws PortalException {

		Group depotGroup = depotEntry.getGroup();

		return ListUtil.fromArray(
			new FDSActionDropdownItem(
				PortletURLBuilder.create(
					PortalUtil.getControlPanelPortletURL(
						httpServletRequest, depotGroup,
						LayoutPageTemplateAdminPortletKeys.
							LAYOUT_PAGE_TEMPLATES,
						0, 0, PortletRequest.RENDER_PHASE)
				).setBackURL(
					backURL
				).setTabs1(
					"master-layouts"
				).buildString(),
				"view", "view", LanguageUtil.get(httpServletRequest, "view"),
				null, null, "link"));
	}

	@Override
	public String getIcon() {
		return "page";
	}

	@Override
	public String getKey() {
		return "master-page";
	}

	@Override
	public String getLabel(Locale locale) {
		return LanguageUtil.get(locale, "master-page");
	}

	@Override
	public Map<String, String> getTypeFilters() {
		return HashMapBuilder.put(
			"type",
			String.valueOf(LayoutPageTemplateEntryTypeConstants.MASTER_LAYOUT)
		).build();
	}

	@Override
	public boolean hasAddPermission(
		PermissionChecker permissionChecker, DepotEntry depotEntry) {

		return _portletResourcePermission.contains(
			permissionChecker, depotEntry.getGroupId(),
			LayoutPageTemplateActionKeys.ADD_LAYOUT_PAGE_TEMPLATE_ENTRY);
	}

	@Override
	public boolean hasViewPermission(
		PermissionChecker permissionChecker, DepotEntry depotEntry) {

		return _portletResourcePermission.contains(
			permissionChecker, depotEntry.getGroupId(), ActionKeys.VIEW);
	}

	@Reference(
		target = "(resource.name=" + LayoutPageTemplateConstants.RESOURCE_NAME + ")"
	)
	private PortletResourcePermission _portletResourcePermission;

}