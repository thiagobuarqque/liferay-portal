/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.style.book.exportimport.data.handler.test;

import com.liferay.exportimport.kernel.lar.ExportImportDateUtil;
import com.liferay.exportimport.kernel.lar.PortletDataHandlerControl;
import com.liferay.exportimport.kernel.lar.PortletDataHandlerKeys;
import com.liferay.exportimport.test.util.exportimport.data.handler.BaseSiteBatchEnginePortletDataHandlerTestCase;
import com.liferay.headless.admin.site.dto.v1_0.StyleBook;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.test.rule.Inject;
import com.liferay.style.book.constants.StyleBookConstants;
import com.liferay.style.book.model.StyleBookEntry;
import com.liferay.style.book.service.StyleBookEntryLocalService;
import com.liferay.style.book.test.util.FrontendTokenDefinitionTestUtil;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Thiago Buarque
 */
public abstract class BaseStyleBookBatchEnginePortletDataHandlerTestCase
	extends BaseSiteBatchEnginePortletDataHandlerTestCase {

	@Test
	public void testExportImportStyleBookFields() throws Exception {
		String frontendTokenName = RandomTestUtil.randomString();

		String frontendTokenDefinition =
			FrontendTokenDefinitionTestUtil.getFrontendTokenDefinition(
				frontendTokenName);
		String frontendTokensValues = JSONUtil.put(
			StringBundler.concat(
				StyleBookConstants.FRONTEND_TOKEN_DEFINITION_ID_CUSTOM,
				StringPool.COLON, frontendTokenName),
			JSONUtil.put("value", RandomTestUtil.randomString())
		).toString();

		Group group = getGroup();

		StyleBookEntry styleBookEntry =
			_styleBookEntryLocalService.addStyleBookEntry(
				RandomTestUtil.randomString(), TestPropsValues.getUserId(),
				group.getGroupId(), true, frontendTokenDefinition,
				frontendTokensValues, RandomTestUtil.randomString(),
				RandomTestUtil.randomString(), _THEME_ID_CLASSIC,
				ServiceContextTestUtil.getServiceContext(
					group.getGroupId(), TestPropsValues.getUserId()));

		_exportImport();

		Group targetGroup = getTargetGroup();

		StyleBookEntry importedStyleBookEntry = _getStyleBookEntry(
			styleBookEntry.getExternalReferenceCode(),
			targetGroup.getGroupId());

		Assert.assertEquals(
			frontendTokenDefinition,
			importedStyleBookEntry.getFrontendTokenDefinition());
		Assert.assertEquals(
			frontendTokensValues,
			importedStyleBookEntry.getFrontendTokensValues());
		Assert.assertEquals(
			styleBookEntry.getName(), importedStyleBookEntry.getName());
		Assert.assertEquals(
			styleBookEntry.getStyleBookEntryKey(),
			importedStyleBookEntry.getStyleBookEntryKey());
		Assert.assertEquals(
			_THEME_ID_CLASSIC, importedStyleBookEntry.getThemeId());
		Assert.assertTrue(importedStyleBookEntry.isDefaultStyleBookEntry());
	}

	@Override
	protected void addEmptyEntry(String externalReferenceCode, long groupId)
		throws Exception {

		_styleBookEntryLocalService.getOrAddEmptyStyleBookEntry(
			externalReferenceCode, TestPropsValues.getUserId(), groupId,
			_THEME_ID_CLASSIC);
	}

	@Override
	protected void addEntry(String externalReferenceCode, long groupId)
		throws Exception {

		_addStyleBookEntry(
			externalReferenceCode, groupId, RandomTestUtil.randomString(),
			StringPool.BLANK, _THEME_ID_CLASSIC);
	}

	@Override
	protected void deleteEntry(String externalReferenceCode, long groupId)
		throws Exception {

		_styleBookEntryLocalService.deleteStyleBookEntry(
			externalReferenceCode, groupId);
	}

	@Override
	protected long getCreatorUserId(String externalReferenceCode, long groupId)
		throws Exception {

		StyleBookEntry styleBookEntry = _getStyleBookEntry(
			externalReferenceCode, groupId);

		return styleBookEntry.getUserId();
	}

	@Override
	protected String getEntityClassName() {
		return StyleBook.class.getName();
	}

	@Override
	protected long getPrimaryKey(String externalReferenceCode, long groupId)
		throws Exception {

		StyleBookEntry styleBookEntry = _getStyleBookEntry(
			externalReferenceCode, groupId);

		return styleBookEntry.getStyleBookEntryId();
	}

	@Override
	protected int getStatus(String externalReferenceCode, long groupId)
		throws Exception {

		StyleBookEntry styleBookEntry = _getStyleBookEntry(
			externalReferenceCode, groupId);

		return styleBookEntry.getStatus();
	}

	@Override
	protected boolean hasEntry(String externalReferenceCode, long groupId) {
		StyleBookEntry styleBookEntry =
			_styleBookEntryLocalService.
				fetchStyleBookEntryByExternalReferenceCode(
					externalReferenceCode, groupId);

		if (styleBookEntry == null) {
			return false;
		}

		return true;
	}

	@Override
	protected boolean supportsComments() {
		return false;
	}

	@Override
	protected boolean supportsEmptyEntries() {
		return true;
	}

	@Override
	protected boolean supportsPermissions() {
		return false;
	}

	@Override
	protected void updateCreatorUserId(
			String externalReferenceCode, long groupId, long userId)
		throws Exception {

		StyleBookEntry draftStyleBookEntry =
			_styleBookEntryLocalService.getDraft(
				_getStyleBookEntry(externalReferenceCode, groupId));

		draftStyleBookEntry.setUserId(userId);

		_styleBookEntryLocalService.publishDraft(draftStyleBookEntry);
	}

	@Override
	protected void updateEntry(String externalReferenceCode, long groupId)
		throws Exception {

		StyleBookEntry styleBookEntry = _getStyleBookEntry(
			externalReferenceCode, groupId);

		_styleBookEntryLocalService.updateStyleBookEntry(
			TestPropsValues.getUserId(), styleBookEntry.getStyleBookEntryId(),
			styleBookEntry.isDefaultStyleBookEntry(),
			styleBookEntry.getFrontendTokenDefinition(),
			styleBookEntry.getFrontendTokensValues(),
			RandomTestUtil.randomString(),
			styleBookEntry.getStyleBookEntryKey(),
			styleBookEntry.getPreviewFileEntryId(), styleBookEntry.getThemeId(),
			ServiceContextTestUtil.getServiceContext(
				groupId, TestPropsValues.getUserId()));
	}

	@Override
	protected void updateModifiedDate(
			String externalReferenceCode, long groupId, Date modifiedDate)
		throws Exception {

		StyleBookEntry draftStyleBookEntry =
			_styleBookEntryLocalService.getDraft(
				_getStyleBookEntry(externalReferenceCode, groupId));

		draftStyleBookEntry.setModifiedDate(modifiedDate);

		_styleBookEntryLocalService.publishDraft(draftStyleBookEntry);
	}

	private StyleBookEntry _addStyleBookEntry(
			String externalReferenceCode, long groupId, String name,
			String styleBookEntryKey, String themeId)
		throws Exception {

		return _styleBookEntryLocalService.addStyleBookEntry(
			externalReferenceCode, TestPropsValues.getUserId(), groupId, false,
			StringPool.BLANK, StringPool.BLANK, name, styleBookEntryKey,
			themeId,
			ServiceContextTestUtil.getServiceContext(
				groupId, TestPropsValues.getUserId()));
	}

	private void _exportImport() throws Exception {
		exportImport(
			null,
			HashMapBuilder.put(
				ExportImportDateUtil.RANGE,
				new String[] {ExportImportDateUtil.RANGE_ALL}
			).put(
				PortletDataHandlerControl.getNamespacedName(
					portletId, StyleBookEntry.class.getName()),
				new String[] {Boolean.TRUE.toString()}
			).put(
				PortletDataHandlerKeys.DATA_STRATEGY,
				new String[] {PortletDataHandlerKeys.DATA_STRATEGY_MIRROR}
			).put(
				PortletDataHandlerKeys.PORTLET_DATA,
				new String[] {Boolean.TRUE.toString()}
			).put(
				PortletDataHandlerKeys.PORTLET_DATA + StringPool.UNDERLINE +
					portletId,
				new String[] {Boolean.TRUE.toString()}
			).build(),
			null);
	}

	private StyleBookEntry _getStyleBookEntry(
			String externalReferenceCode, long groupId)
		throws Exception {

		return _styleBookEntryLocalService.
			getStyleBookEntryByExternalReferenceCode(
				externalReferenceCode, groupId);
	}

	private static final String _THEME_ID_CLASSIC = "classic_WAR_classictheme";

	@Inject
	private StyleBookEntryLocalService _styleBookEntryLocalService;

}