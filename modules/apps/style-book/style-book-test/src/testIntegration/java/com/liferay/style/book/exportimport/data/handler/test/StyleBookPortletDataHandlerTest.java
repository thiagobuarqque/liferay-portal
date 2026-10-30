/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.style.book.exportimport.data.handler.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.depot.constants.DepotConstants;
import com.liferay.depot.model.DepotEntry;
import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.exportimport.constants.ExportImportConstants;
import com.liferay.exportimport.kernel.configuration.ExportImportConfigurationParameterMapFactoryUtil;
import com.liferay.exportimport.kernel.lar.ExportImportHelperUtil;
import com.liferay.exportimport.kernel.lar.PortletDataHandler;
import com.liferay.exportimport.kernel.staging.StagingUtil;
import com.liferay.exportimport.portlet.data.handler.provider.PortletDataHandlerProvider;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Portlet;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.FeatureFlagTestUtil;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.ClassUtil;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.style.book.constants.StyleBookPortletKeys;
import com.liferay.style.book.model.StyleBookEntry;
import com.liferay.style.book.service.StyleBookEntryLocalService;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.CoreMatchers;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Thiago Buarque
 */
@RunWith(Arquillian.class)
public class StyleBookPortletDataHandlerTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testEnableLocalStaging() throws Exception {
		_group = GroupTestUtil.addGroup();

		StyleBookEntry styleBookEntry =
			_styleBookEntryLocalService.addStyleBookEntry(
				RandomTestUtil.randomString(), TestPropsValues.getUserId(),
				_group.getGroupId(), false, StringPool.BLANK, StringPool.BLANK,
				RandomTestUtil.randomString(), StringPool.BLANK,
				"classic_WAR_classictheme",
				ServiceContextTestUtil.getServiceContext(
					_group.getGroupId(), TestPropsValues.getUserId()));

		GroupTestUtil.enableLocalStaging(_group);

		Assert.assertFalse(
			_group.isStagedPortlet(StyleBookPortletKeys.STYLE_BOOK));

		Group stagingGroup = _group.getStagingGroup();

		Assert.assertNull(
			_styleBookEntryLocalService.
				fetchStyleBookEntryByExternalReferenceCode(
					styleBookEntry.getExternalReferenceCode(),
					stagingGroup.getGroupId()));
	}

	@FeatureFlag("LPD-57283")
	@Test
	public void testGetExportablePortlets() throws Exception {
		FeatureFlagTestUtil.invokeFeatureFlagListeners(
			TestPropsValues.getCompanyId(), true, "LPD-57283");

		_group = GroupTestUtil.addGroup();

		Assert.assertTrue(_hasStyleBookPortlet(_group.getGroupId()));

		DepotEntry designLibraryDepotEntry = _addDepotEntry(
			DepotConstants.TYPE_DESIGN_LIBRARY);

		Assert.assertTrue(
			_hasStyleBookPortlet(designLibraryDepotEntry.getGroupId()));

		DepotEntry assetLibraryDepotEntry = _addDepotEntry(
			DepotConstants.TYPE_ASSET_LIBRARY);

		Assert.assertFalse(
			_hasStyleBookPortlet(assetLibraryDepotEntry.getGroupId()));
	}

	@FeatureFlag(enable = false, value = "LPD-57283")
	@Test
	public void testGetExportablePortletsWithFeatureFlagDisabled()
		throws Exception {

		_group = GroupTestUtil.addGroup();

		Assert.assertTrue(_hasStyleBookPortlet(_group.getGroupId()));

		DepotEntry designLibraryDepotEntry = _addDepotEntry(
			DepotConstants.TYPE_DESIGN_LIBRARY);

		Assert.assertFalse(
			_hasStyleBookPortlet(designLibraryDepotEntry.getGroupId()));
	}

	@Test
	public void testPortletDataHandlerRegistration() throws Exception {
		PortletDataHandler portletDataHandler =
			_portletDataHandlerProvider.provide(
				TestPropsValues.getCompanyId(),
				StyleBookPortletKeys.STYLE_BOOK);

		Assert.assertThat(
			ClassUtil.getClassName(portletDataHandler),
			CoreMatchers.containsString("BatchEnginePortletDataHandler"));
		Assert.assertEquals(
			StyleBookEntry.class.getName(),
			portletDataHandler.getClassNames()[0]);
		Assert.assertEquals(
			ExportImportConstants.SECTION_KEY_DESIGN,
			portletDataHandler.getSectionKey());
		Assert.assertFalse(portletDataHandler.isStaged());
	}

	@Test
	public void testPublishLayouts() throws Exception {
		_group = GroupTestUtil.addGroup();

		StyleBookEntry liveStyleBookEntry = _addStyleBookEntry(
			_group.getGroupId());

		GroupTestUtil.enableLocalStaging(_group);

		Group stagingGroup = _group.getStagingGroup();

		StyleBookEntry stagingStyleBookEntry = _addStyleBookEntry(
			stagingGroup.getGroupId());

		StagingUtil.publishLayouts(
			TestPropsValues.getUserId(), stagingGroup.getGroupId(),
			_group.getGroupId(), false,
			ExportImportConfigurationParameterMapFactoryUtil.
				buildFullPublishParameterMap());

		Assert.assertNull(
			_styleBookEntryLocalService.
				fetchStyleBookEntryByExternalReferenceCode(
					stagingStyleBookEntry.getExternalReferenceCode(),
					_group.getGroupId()));

		StyleBookEntry publishedStyleBookEntry =
			_styleBookEntryLocalService.
				fetchStyleBookEntryByExternalReferenceCode(
					liveStyleBookEntry.getExternalReferenceCode(),
					_group.getGroupId());

		Assert.assertEquals(
			liveStyleBookEntry.getName(), publishedStyleBookEntry.getName());
	}

	@Rule
	public final PermissionCheckerMethodTestRule
		permissionCheckerMethodTestRule =
			PermissionCheckerMethodTestRule.INSTANCE;

	private DepotEntry _addDepotEntry(int type) throws Exception {
		DepotEntry depotEntry = _depotEntryLocalService.addDepotEntry(
			RandomTestUtil.randomLocaleStringMap(),
			RandomTestUtil.randomLocaleStringMap(), type,
			ServiceContextTestUtil.getServiceContext());

		_depotEntries.add(depotEntry);

		return depotEntry;
	}

	private StyleBookEntry _addStyleBookEntry(long groupId) throws Exception {
		return _styleBookEntryLocalService.addStyleBookEntry(
			RandomTestUtil.randomString(), TestPropsValues.getUserId(), groupId,
			false, StringPool.BLANK, StringPool.BLANK,
			RandomTestUtil.randomString(), StringPool.BLANK,
			"classic_WAR_classictheme",
			ServiceContextTestUtil.getServiceContext(
				groupId, TestPropsValues.getUserId()));
	}

	private boolean _hasStyleBookPortlet(long groupId) throws Exception {
		List<Portlet> portlets = ExportImportHelperUtil.getExportablePortlets(
			TestPropsValues.getCompanyId(), false, groupId);

		List<String> rootPortletIds = TransformUtil.transform(
			portlets, Portlet::getRootPortletId);

		return rootPortletIds.contains(StyleBookPortletKeys.STYLE_BOOK);
	}

	@DeleteAfterTestRun
	private final List<DepotEntry> _depotEntries = new ArrayList<>();

	@Inject
	private DepotEntryLocalService _depotEntryLocalService;

	@DeleteAfterTestRun
	private Group _group;

	@Inject
	private PortletDataHandlerProvider _portletDataHandlerProvider;

	@Inject
	private StyleBookEntryLocalService _styleBookEntryLocalService;

}