/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.language.override.rest.internal.resource.v1_0;

import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.OrderByComparatorFactoryUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.language.override.model.PLOEntry;
import com.liferay.portal.language.override.rest.dto.v1_0.LanguageOverride;
import com.liferay.portal.language.override.rest.internal.dto.v1_0.util.CreatorUtil;
import com.liferay.portal.language.override.rest.resource.v1_0.LanguageOverrideResource;
import com.liferay.portal.language.override.service.PLOEntryService;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Thiago Buarque
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/language-override.properties",
	scope = ServiceScope.PROTOTYPE, service = LanguageOverrideResource.class
)
public class LanguageOverrideResourceImpl
	extends BaseLanguageOverrideResourceImpl {

	@Override
	public void deleteLanguageOverride(String key, String languageId)
		throws Exception {

		_ploEntryService.deletePLOEntry(key, languageId);
	}

	@Override
	public Page<LanguageOverride> getLanguageOverridesPage(
			String keywords, Pagination pagination, Sort[] sorts)
		throws Exception {

		long companyId = contextCompany.getCompanyId();

		return Page.of(
			transform(
				_ploEntryService.getPLOEntries(
					companyId, keywords, pagination.getStartPosition(),
					pagination.getEndPosition(), _toOrderByComparator(sorts)),
				this::_toLanguageOverride),
			pagination,
			_ploEntryService.getPLOEntriesCount(companyId, keywords));
	}

	@Override
	public LanguageOverride postLanguageOverride(
			LanguageOverride languageOverride)
		throws Exception {

		return _toLanguageOverride(
			_ploEntryService.addOrUpdatePLOEntry(
				languageOverride.getKey(), languageOverride.getLanguageId(),
				languageOverride.getValue(), languageOverride.getDateCreated(),
				languageOverride.getDateModified()));
	}

	private LanguageOverride _toLanguageOverride(PLOEntry ploEntry) {
		return new LanguageOverride() {
			{
				setCreator(
					() -> CreatorUtil.toCreator(
						_portal,
						_userLocalService.fetchUser(ploEntry.getUserId())));
				setDateCreated(ploEntry::getCreateDate);
				setDateModified(ploEntry::getModifiedDate);
				setKey(ploEntry::getKey);
				setLanguageId(ploEntry::getLanguageId);
				setValue(ploEntry::getValue);
			}
		};
	}

	private OrderByComparator<PLOEntry> _toOrderByComparator(Sort[] sorts) {
		if (ArrayUtil.isEmpty(sorts)) {
			return null;
		}

		Sort sort = sorts[0];

		String fieldName = sort.getFieldName();

		if (Validator.isNull(fieldName)) {
			return null;
		}

		return OrderByComparatorFactoryUtil.create(
			"PLOEntry", fieldName, !sort.isReverse());
	}

	@Reference
	private PLOEntryService _ploEntryService;

	@Reference
	private Portal _portal;

	@Reference
	private UserLocalService _userLocalService;

}