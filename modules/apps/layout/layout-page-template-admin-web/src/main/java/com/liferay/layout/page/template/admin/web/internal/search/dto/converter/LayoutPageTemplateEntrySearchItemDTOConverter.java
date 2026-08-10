/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.page.template.admin.web.internal.search.dto.converter;

import com.liferay.layout.page.template.admin.web.internal.search.dto.LayoutPageTemplateEntrySearchItem;
import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DTOConverterContext;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Names the payload <code>/o/search</code> embeds in a layout page template
 * entry row.
 *
 * <p>
 * Several converters are registered for this entry class name, one per headless
 * representation, and the registry keeps a single one. This converter outranks
 * them so that every row carries the same shape whichever variation it is,
 * which the Design Library Admin needs to tell them apart. Ranking it is safe
 * because no representation registers a
 * <code>VulcanCRUDItemDelegate</code>, so rows embed nothing at all today.
 * </p>
 *
 * @author Thiago Buarque
 */
@Component(
	property = {
		"dto.class.name=com.liferay.layout.page.template.model.LayoutPageTemplateEntry",
		"service.ranking:Integer=1000"
	},
	service = DTOConverter.class
)
public class LayoutPageTemplateEntrySearchItemDTOConverter
	implements DTOConverter
		<LayoutPageTemplateEntry, LayoutPageTemplateEntrySearchItem> {

	@Override
	public String getContentType() {
		return LayoutPageTemplateEntrySearchItem.class.getSimpleName();
	}

	@Override
	public LayoutPageTemplateEntrySearchItem toDTO(
		DTOConverterContext dtoConverterContext,
		LayoutPageTemplateEntry layoutPageTemplateEntry) {

		return new LayoutPageTemplateEntrySearchItem(
			layoutPageTemplateEntry,
			_userLocalService.fetchUser(layoutPageTemplateEntry.getUserId()));
	}

	@Reference
	private UserLocalService _userLocalService;

}