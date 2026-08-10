/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.page.template.admin.web.internal.search.dto;

import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.portal.kernel.model.User;

/**
 * The payload embedded in a <code>/o/search</code> row for a layout page
 * template entry.
 *
 * <p>
 * Masters, display page templates, content page templates, and widget page
 * templates share one entry class name, so a row cannot be attributed to one of
 * them without <code>type</code>. Search returns no type of its own, which is
 * why this payload carries it.
 * </p>
 *
 * @author Thiago Buarque
 */
public class LayoutPageTemplateEntrySearchItem {

	public LayoutPageTemplateEntrySearchItem(
		LayoutPageTemplateEntry layoutPageTemplateEntry, User user) {

		_creator = new Creator(
			(user == null) ? layoutPageTemplateEntry.getUserName() :
				user.getFullName());
		_externalReferenceCode =
			layoutPageTemplateEntry.getExternalReferenceCode();
		_id = layoutPageTemplateEntry.getLayoutPageTemplateEntryId();
		_name = layoutPageTemplateEntry.getName();
		_type = layoutPageTemplateEntry.getType();
	}

	public Creator getCreator() {
		return _creator;
	}

	public String getExternalReferenceCode() {
		return _externalReferenceCode;
	}

	public long getId() {
		return _id;
	}

	public String getName() {
		return _name;
	}

	public int getType() {
		return _type;
	}

	public static class Creator {

		public Creator(String name) {
			_name = name;
		}

		public String getName() {
			return _name;
		}

		private final String _name;

	}

	private final Creator _creator;
	private final String _externalReferenceCode;
	private final long _id;
	private final String _name;
	private final int _type;

}