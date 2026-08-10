/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.page.template.admin.web.internal.search.crud;

import com.liferay.layout.page.template.admin.web.internal.search.dto.LayoutPageTemplateEntrySearchItem;
import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.layout.page.template.service.LayoutPageTemplateEntryService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.vulcan.crud.VulcanCRUDItemDelegate;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Builds the payload <code>/o/search</code> embeds in a layout page template
 * entry row. Without a delegate registered for the payload's class name,
 * <code>/o/search</code> returns the row with no embedded fields at all and it
 * lists with empty columns.
 *
 * @author Thiago Buarque
 */
@Component(
	property = {
		"crud.entity.class.name=com.liferay.layout.page.template.admin.web.internal.search.dto.LayoutPageTemplateEntrySearchItem",
		"crud.item.delegate=true"
	},
	service = VulcanCRUDItemDelegate.class
)
public class LayoutPageTemplateEntrySearchItemDelegate
	implements VulcanCRUDItemDelegate<LayoutPageTemplateEntrySearchItem> {

	@Override
	public LayoutPageTemplateEntrySearchItem getItem(Long id) throws Exception {
		LayoutPageTemplateEntry layoutPageTemplateEntry =
			_layoutPageTemplateEntryService.getLayoutPageTemplateEntry(id);

		return new LayoutPageTemplateEntrySearchItem(
			layoutPageTemplateEntry,
			_userLocalService.fetchUser(layoutPageTemplateEntry.getUserId()));
	}

	@Reference
	private LayoutPageTemplateEntryService _layoutPageTemplateEntryService;

	@Reference
	private UserLocalService _userLocalService;

}