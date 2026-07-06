/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.internal.dto.v1_0.converter;

import com.liferay.document.library.kernel.service.DLAppLocalService;
import com.liferay.headless.admin.site.dto.v1_0.StyleBook;
import com.liferay.headless.admin.user.dto.v1_0.Creator;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DTOConverterContext;
import com.liferay.style.book.model.StyleBookEntry;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Thiago Buarque
 */
@Component(service = DTOConverter.class)
public class StyleBookDTOConverter
	implements DTOConverter<StyleBookEntry, StyleBook> {

	@Override
	public String getContentType() {
		return "StyleBook";
	}

	@Override
	public StyleBook toDTO(
		DTOConverterContext dtoConverterContext,
		StyleBookEntry styleBookEntry) {

		StyleBook styleBook = new StyleBook();

		styleBook.setCreator(
			() -> {
				User user = _userLocalService.fetchUser(
					styleBookEntry.getUserId());

				if (user == null) {
					return null;
				}

				return new Creator() {
					{
						setExternalReferenceCode(
							user::getExternalReferenceCode);
					}
				};
			});
		styleBook.setDateCreated(styleBookEntry::getCreateDate);
		styleBook.setDateModified(styleBookEntry::getModifiedDate);
		styleBook.setDefaultStyleBook(styleBookEntry::getDefaultStyleBookEntry);
		styleBook.setExternalReferenceCode(
			styleBookEntry::getExternalReferenceCode);
		styleBook.setFrontendTokensValues(
			styleBookEntry::getFrontendTokensValues);
		styleBook.setKey(styleBookEntry::getStyleBookEntryKey);
		styleBook.setName(styleBookEntry::getName);
		styleBook.setPreviewFileEntryExternalReferenceCode(
			() -> {
				long previewFileEntryId =
					styleBookEntry.getPreviewFileEntryId();

				if (previewFileEntryId == 0) {
					return null;
				}

				FileEntry fileEntry = _dlAppLocalService.getFileEntry(
					previewFileEntryId);

				if (fileEntry == null) {
					return null;
				}

				return fileEntry.getExternalReferenceCode();
			});
		styleBook.setThemeId(styleBookEntry::getThemeId);

		return styleBook;
	}

	@Reference
	private DLAppLocalService _dlAppLocalService;

	@Reference
	private UserLocalService _userLocalService;

}