/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.depot.constants.DepotConstants;
import com.liferay.depot.model.DepotEntry;
import com.liferay.depot.service.DepotEntryGroupRelLocalService;
import com.liferay.depot.service.DepotEntryLocalServiceUtil;
import com.liferay.headless.admin.site.client.dto.v1_0.StyleBook;
import com.liferay.headless.admin.site.client.pagination.Page;
import com.liferay.headless.admin.site.client.pagination.Pagination;
import com.liferay.headless.admin.site.client.problem.Problem;
import com.liferay.headless.admin.site.client.resource.v1_0.StyleBookResource;
import com.liferay.headless.admin.site.resource.v1_0.test.util.LayoutPageTemplateEntryTestUtil;
import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.style.book.model.StyleBookEntry;
import com.liferay.style.book.service.StyleBookEntryLocalServiceUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Rubén Pulido
 * @author Thiago Buarque
 */
@FeatureFlag("LPD-57283")
@RunWith(Arquillian.class)
public class StyleBookResourceTest extends BaseStyleBookResourceTestCase {

	@Test
	public void testGetDesignLibraryStyleBooksPageWhenDepotTypeIsDesignLibrary()
		throws Exception {

		Group designLibraryGroup = _addDepotGroup(
			DepotConstants.TYPE_DESIGN_LIBRARY);

		Page<StyleBook> page = styleBookResource.getDesignLibraryStyleBooksPage(
			designLibraryGroup.getExternalReferenceCode(), null, null, null,
			Pagination.of(1, 10), null);

		Assert.assertEquals(0, page.getTotalCount());
	}

	@Test
	public void testGetDesignLibraryStyleBooksPageWhenDepotTypeIsNotDesignLibrary()
		throws Exception {

		Group assetLibraryGroup = _addDepotGroup(
			DepotConstants.TYPE_ASSET_LIBRARY);

		try {
			styleBookResource.getDesignLibraryStyleBooksPage(
				assetLibraryGroup.getExternalReferenceCode(), null, null, null,
				Pagination.of(1, 10), null);

			Assert.fail();
		}
		catch (Problem.ProblemException problemException) {
			Problem problem = problemException.getProblem();

			Assert.assertEquals("NOT_FOUND", problem.getStatus());
		}
	}

	@Test
	public void testGetSitePageSpecificationStyleBooksPageForContentPage()
		throws Exception {

		Layout layout = LayoutTestUtil.addTypeContentLayout(testGroup);

		layout = _layoutLocalService.updateLookAndFeel(
			layout.getGroupId(), layout.isPrivateLayout(), layout.getLayoutId(),
			_THEME_ID_CLASSIC, "01", StringPool.BLANK);

		StyleBookEntry matchingStyleBookEntry = _addStyleBookEntry(
			testGroup, _THEME_ID_CLASSIC);
		StyleBookEntry otherThemeStyleBookEntry = _addStyleBookEntry(
			testGroup, _THEME_ID_DIALECT);

		Page<StyleBook> page =
			styleBookResource.getSitePageSpecificationStyleBooksPage(
				testGroup.getExternalReferenceCode(),
				layout.getExternalReferenceCode(), null, Pagination.of(1, 10));

		Set<String> externalReferenceCodes = _getExternalReferenceCodes(page);

		Assert.assertTrue(
			externalReferenceCodes.toString(),
			externalReferenceCodes.contains(
				matchingStyleBookEntry.getExternalReferenceCode()));
		Assert.assertFalse(
			externalReferenceCodes.toString(),
			externalReferenceCodes.contains(
				otherThemeStyleBookEntry.getExternalReferenceCode()));
	}

	@Test
	public void testGetSitePageSpecificationStyleBooksPageForDraftAndPublishedLayouts()
		throws Exception {

		Layout publishedLayout = LayoutTestUtil.addTypeContentLayout(testGroup);

		Layout draftLayout = publishedLayout.fetchDraftLayout();

		publishedLayout = _layoutLocalService.updateLookAndFeel(
			publishedLayout.getGroupId(), publishedLayout.isPrivateLayout(),
			publishedLayout.getLayoutId(), _THEME_ID_CLASSIC, "01",
			StringPool.BLANK);

		draftLayout = _layoutLocalService.updateLookAndFeel(
			draftLayout.getGroupId(), draftLayout.isPrivateLayout(),
			draftLayout.getLayoutId(), _THEME_ID_DIALECT, "01",
			StringPool.BLANK);

		StyleBookEntry classicStyleBookEntry = _addStyleBookEntry(
			testGroup, _THEME_ID_CLASSIC);
		StyleBookEntry dialectStyleBookEntry = _addStyleBookEntry(
			testGroup, _THEME_ID_DIALECT);

		Set<String> publishedExternalReferenceCodes =
			_getExternalReferenceCodes(
				styleBookResource.getSitePageSpecificationStyleBooksPage(
					testGroup.getExternalReferenceCode(),
					publishedLayout.getExternalReferenceCode(), null,
					Pagination.of(1, 10)));
		Set<String> draftExternalReferenceCodes = _getExternalReferenceCodes(
			styleBookResource.getSitePageSpecificationStyleBooksPage(
				testGroup.getExternalReferenceCode(),
				draftLayout.getExternalReferenceCode(), null,
				Pagination.of(1, 10)));

		Assert.assertTrue(
			publishedExternalReferenceCodes.toString(),
			publishedExternalReferenceCodes.contains(
				classicStyleBookEntry.getExternalReferenceCode()));
		Assert.assertFalse(
			publishedExternalReferenceCodes.toString(),
			publishedExternalReferenceCodes.contains(
				dialectStyleBookEntry.getExternalReferenceCode()));

		Assert.assertTrue(
			draftExternalReferenceCodes.toString(),
			draftExternalReferenceCodes.contains(
				dialectStyleBookEntry.getExternalReferenceCode()));
		Assert.assertFalse(
			draftExternalReferenceCodes.toString(),
			draftExternalReferenceCodes.contains(
				classicStyleBookEntry.getExternalReferenceCode()));

		Assert.assertNotEquals(
			publishedExternalReferenceCodes, draftExternalReferenceCodes);
	}

	@Test
	public void testGetSitePageSpecificationStyleBooksPageForMasterPage()
		throws Exception {

		LayoutPageTemplateEntry layoutPageTemplateEntry =
			LayoutPageTemplateEntryTestUtil.getMasterLayoutPageTemplateEntry(
				ServiceContextTestUtil.getServiceContext(
					testGroup, TestPropsValues.getUserId()),
				WorkflowConstants.STATUS_APPROVED);

		Layout layout = _layoutLocalService.getLayout(
			layoutPageTemplateEntry.getPlid());

		_layoutLocalService.updateLookAndFeel(
			layout.getGroupId(), layout.isPrivateLayout(), layout.getLayoutId(),
			_THEME_ID_CLASSIC, "01", StringPool.BLANK);

		StyleBookEntry styleBookEntry = _addStyleBookEntry(
			testGroup, _THEME_ID_CLASSIC);

		Page<StyleBook> page =
			styleBookResource.getSitePageSpecificationStyleBooksPage(
				testGroup.getExternalReferenceCode(),
				layoutPageTemplateEntry.getExternalReferenceCode(), null,
				Pagination.of(1, 10));

		Set<String> externalReferenceCodes = _getExternalReferenceCodes(page);

		Assert.assertTrue(
			externalReferenceCodes.toString(),
			externalReferenceCodes.contains(
				styleBookEntry.getExternalReferenceCode()));
	}

	@Test
	public void testGetSitePageSpecificationStyleBooksPageIncludesConnectedDesignLibraryStyleBooks()
		throws Exception {

		Layout layout = LayoutTestUtil.addTypeContentLayout(testGroup);

		layout = _layoutLocalService.updateLookAndFeel(
			layout.getGroupId(), layout.isPrivateLayout(), layout.getLayoutId(),
			_THEME_ID_CLASSIC, "01", StringPool.BLANK);

		Group connectedDepotGroup = _addDesignLibraryDepotGroup();

		_connectDepotGroup(connectedDepotGroup, testGroup);

		StyleBookEntry connectedStyleBookEntry = _addStyleBookEntry(
			connectedDepotGroup, _THEME_ID_CLASSIC);

		Group unconnectedDepotGroup = _addDesignLibraryDepotGroup();

		StyleBookEntry unconnectedStyleBookEntry = _addStyleBookEntry(
			unconnectedDepotGroup, _THEME_ID_CLASSIC);

		Page<StyleBook> page =
			styleBookResource.getSitePageSpecificationStyleBooksPage(
				testGroup.getExternalReferenceCode(),
				layout.getExternalReferenceCode(), null, Pagination.of(1, 10));

		Set<String> externalReferenceCodes = _getExternalReferenceCodes(page);

		Assert.assertTrue(
			externalReferenceCodes.toString(),
			externalReferenceCodes.contains(
				connectedStyleBookEntry.getExternalReferenceCode()));
		Assert.assertFalse(
			externalReferenceCodes.toString(),
			externalReferenceCodes.contains(
				unconnectedStyleBookEntry.getExternalReferenceCode()));
	}

	@FeatureFlag(enable = false, value = "LPD-57283")
	@Test
	public void testGetSitePageSpecificationStyleBooksPageWhenFeatureFlagDisabled()
		throws Exception {

		Layout layout = LayoutTestUtil.addTypeContentLayout(testGroup);

		try {
			styleBookResource.getSitePageSpecificationStyleBooksPage(
				testGroup.getExternalReferenceCode(),
				layout.getExternalReferenceCode(), null, Pagination.of(1, 10));

			Assert.fail();
		}
		catch (Problem.ProblemException problemException) {
			Problem problem = problemException.getProblem();

			Assert.assertEquals("BAD_REQUEST", problem.getStatus());
		}
	}

	@Test
	public void testGetSitePageSpecificationStyleBooksPageWithoutPermission()
		throws Exception {

		Layout layout = LayoutTestUtil.addTypeContentLayout(testGroup);

		User user = UserTestUtil.addUser(false);

		user = _userLocalService.updatePassword(
			user.getUserId(), "test", "test", false, true);

		StyleBookResource restrictedStyleBookResource =
			StyleBookResource.builder(
			).authentication(
				user.getEmailAddress(), "test"
			).endpoint(
				testCompany.getVirtualHostname(),
				PortalUtil.getPortalServerPort(false), "http"
			).locale(
				LocaleUtil.getDefault()
			).build();

		try {
			restrictedStyleBookResource.getSitePageSpecificationStyleBooksPage(
				testGroup.getExternalReferenceCode(),
				layout.getExternalReferenceCode(), null, Pagination.of(1, 10));

			Assert.fail();
		}
		catch (Problem.ProblemException problemException) {
			Problem problem = problemException.getProblem();

			// GET requests mask PrincipalException as NOT_FOUND instead of
			// FORBIDDEN

			Assert.assertEquals("NOT_FOUND", problem.getStatus());
		}
	}

	@Override
	@Test
	public void testGetSiteStyleBook() throws Exception {
		super.testGetSiteStyleBook();

		try {
			styleBookResource.getSiteStyleBook(
				testGetSiteStyleBook_getSiteExternalReferenceCode(),
				RandomTestUtil.randomString());

			Assert.fail();
		}
		catch (Problem.ProblemException problemException) {
			Problem problem = problemException.getProblem();

			Assert.assertEquals("NOT_FOUND", problem.getStatus());
		}
	}

	@Override
	@Test
	public void testPatchSiteStyleBook() throws Exception {
		_testPatchSiteStyleBook();
		_testPatchSiteStyleBookForNonexistingStyleBook();
	}

	@Override
	@Test
	public void testPostSiteStyleBook() throws Exception {
		super.testPostSiteStyleBook();

		_testPostSiteStyleBookWithBlankThemeId();
		_testPostSiteStyleBookWithDuplicateExternalReferenceCode();
		_testPostSiteStyleBookWithDuplicateKey();
	}

	@Test
	public void testStyleBookFromDesignLibraryHasDesignLibraryFields()
		throws Exception {

		Group depotGroup = _addDesignLibraryDepotGroup();

		StyleBookEntry styleBookEntry = _addStyleBookEntry(depotGroup);

		StyleBook styleBook = styleBookResource.getSiteStyleBook(
			depotGroup.getExternalReferenceCode(),
			styleBookEntry.getExternalReferenceCode());

		Assert.assertEquals(
			depotGroup.getExternalReferenceCode(),
			styleBook.getDesignLibraryExternalReferenceCode());
		Assert.assertEquals(
			depotGroup.getDescriptiveName(LocaleUtil.getDefault()),
			styleBook.getDesignLibraryName());
	}

	@Override
	protected StyleBook testDeleteDesignLibraryStyleBook_addStyleBook()
		throws Exception {

		return _addDesignLibraryStyleBook(randomStyleBook());
	}

	@Override
	protected String
			testDeleteDesignLibraryStyleBook_getDesignLibraryExternalReferenceCode(
				StyleBook styleBook)
		throws Exception {

		return styleBook.getDesignLibraryExternalReferenceCode();
	}

	@Override
	protected StyleBook testGetDesignLibraryStyleBook_addStyleBook()
		throws Exception {

		return _addDesignLibraryStyleBook(randomStyleBook());
	}

	@Override
	protected String
			testGetDesignLibraryStyleBook_getDesignLibraryExternalReferenceCode(
				StyleBook styleBook)
		throws Exception {

		return styleBook.getDesignLibraryExternalReferenceCode();
	}

	@Override
	protected StyleBook testGetDesignLibraryStyleBooksPage_addStyleBook(
			String designLibraryExternalReferenceCode, StyleBook styleBook)
		throws Exception {

		Group designLibraryGroup =
			_groupLocalService.fetchGroupByExternalReferenceCode(
				designLibraryExternalReferenceCode, testCompany.getCompanyId());

		StyleBookEntry styleBookEntry = _addStyleBookEntry(
			designLibraryGroup, styleBook);

		return styleBookResource.getDesignLibraryStyleBook(
			designLibraryExternalReferenceCode,
			styleBookEntry.getExternalReferenceCode());
	}

	@Override
	protected String
			testGetDesignLibraryStyleBooksPage_getDesignLibraryExternalReferenceCode()
		throws Exception {

		Group designLibraryGroup = _addDesignLibraryDepotGroup();

		return designLibraryGroup.getExternalReferenceCode();
	}

	@Override
	protected StyleBook testGetSitePageSpecificationStyleBooksPage_addStyleBook(
			String siteExternalReferenceCode,
			String pageSpecificationExternalReferenceCode, StyleBook styleBook)
		throws Exception {

		styleBook.setThemeId(_THEME_ID_CLASSIC);

		return styleBookResource.postSiteStyleBook(
			siteExternalReferenceCode, styleBook);
	}

	@Override
	protected String
			testGetSitePageSpecificationStyleBooksPage_getPageSpecificationExternalReferenceCode()
		throws Exception {

		Layout layout = LayoutTestUtil.addTypeContentLayout(testGroup);

		layout = _layoutLocalService.updateLookAndFeel(
			layout.getGroupId(), layout.isPrivateLayout(), layout.getLayoutId(),
			_THEME_ID_CLASSIC, "01", StringPool.BLANK);

		return layout.getExternalReferenceCode();
	}

	@Override
	protected StyleBook testPostSiteStyleBook_addStyleBook(StyleBook styleBook)
		throws Exception {

		return styleBookResource.postSiteStyleBook(
			testGroup.getExternalReferenceCode(), styleBook);
	}

	private Group _addDepotGroup(int type) throws Exception {
		DepotEntry depotEntry = DepotEntryLocalServiceUtil.addDepotEntry(
			Collections.singletonMap(
				LocaleUtil.getDefault(), RandomTestUtil.randomString()),
			null, type,
			new ServiceContext() {
				{
					setCompanyId(testCompany.getCompanyId());
					setUserId(TestPropsValues.getUserId());
				}
			});

		return depotEntry.getGroup();
	}

	private Group _addDesignLibraryDepotGroup() throws Exception {
		return _addDepotGroup(DepotConstants.TYPE_DESIGN_LIBRARY);
	}

	private StyleBook _addDesignLibraryStyleBook(StyleBook styleBook)
		throws Exception {

		Group designLibraryGroup = _addDesignLibraryDepotGroup();

		StyleBookEntry styleBookEntry = _addStyleBookEntry(
			designLibraryGroup, styleBook);

		return styleBookResource.getDesignLibraryStyleBook(
			designLibraryGroup.getExternalReferenceCode(),
			styleBookEntry.getExternalReferenceCode());
	}

	private StyleBookEntry _addStyleBookEntry(Group group) throws Exception {
		return StyleBookEntryLocalServiceUtil.addStyleBookEntry(
			RandomTestUtil.randomString(), TestPropsValues.getUserId(),
			group.getGroupId(), false, StringPool.BLANK,
			RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			RandomTestUtil.randomString(), null);
	}

	private StyleBookEntry _addStyleBookEntry(Group group, String themeId)
		throws Exception {

		return StyleBookEntryLocalServiceUtil.addStyleBookEntry(
			RandomTestUtil.randomString(), TestPropsValues.getUserId(),
			group.getGroupId(), false, StringPool.BLANK,
			RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			themeId, null);
	}

	private StyleBookEntry _addStyleBookEntry(Group group, StyleBook styleBook)
		throws Exception {

		boolean defaultStyleBook = false;

		if ((styleBook.getDefaultStyleBook() != null) &&
			styleBook.getDefaultStyleBook()) {

			defaultStyleBook = true;
		}

		return StyleBookEntryLocalServiceUtil.addStyleBookEntry(
			styleBook.getExternalReferenceCode(), TestPropsValues.getUserId(),
			group.getGroupId(), defaultStyleBook,
			styleBook.getFrontendTokensValues(), styleBook.getName(),
			styleBook.getKey(), styleBook.getThemeId(), null);
	}

	private void _connectDepotGroup(Group depotGroup, Group group)
		throws Exception {

		DepotEntry depotEntry = DepotEntryLocalServiceUtil.fetchGroupDepotEntry(
			depotGroup.getGroupId());

		_depotEntryGroupRelLocalService.addDepotEntryGroupRel(
			depotEntry.getDepotEntryId(), group.getGroupId());
	}

	private Set<String> _getExternalReferenceCodes(Page<StyleBook> page) {
		Set<String> externalReferenceCodes = new HashSet<>();

		for (StyleBook styleBook : page.getItems()) {
			externalReferenceCodes.add(styleBook.getExternalReferenceCode());
		}

		return externalReferenceCodes;
	}

	private void _testPatchSiteStyleBook() throws Exception {
		StyleBook postStyleBook = testPatchSiteStyleBook_addStyleBook();

		StyleBook randomPatchStyleBook = randomPatchStyleBook();

		StyleBook patchStyleBook = styleBookResource.patchSiteStyleBook(
			testGroup.getExternalReferenceCode(),
			postStyleBook.getExternalReferenceCode(), randomPatchStyleBook);

		StyleBook expectedPatchStyleBook = postStyleBook.clone();

		BeanTestUtil.copyProperties(
			randomPatchStyleBook, expectedPatchStyleBook);

		StyleBook getStyleBook = styleBookResource.getSiteStyleBook(
			testGroup.getExternalReferenceCode(),
			patchStyleBook.getExternalReferenceCode());

		assertEquals(expectedPatchStyleBook, getStyleBook);
		assertValid(getStyleBook);
	}

	private void _testPatchSiteStyleBookForNonexistingStyleBook()
		throws Exception {

		try {
			styleBookResource.patchSiteStyleBook(
				testGroup.getExternalReferenceCode(),
				RandomTestUtil.randomString(), randomPatchStyleBook());

			Assert.fail();
		}
		catch (Problem.ProblemException problemException) {
			Problem problem = problemException.getProblem();

			Assert.assertEquals("NOT_FOUND", problem.getStatus());
		}
	}

	private void _testPostSiteStyleBookWithBlankThemeId() throws Exception {
		try {
			StyleBook randomStyleBook = randomStyleBook();

			randomStyleBook.setThemeId(StringPool.BLANK);

			testPostSiteStyleBook_addStyleBook(randomStyleBook);

			Assert.fail();
		}
		catch (Problem.ProblemException problemException) {
			Assert.assertEquals(
				"Theme ID must not be null", problemException.getMessage());
		}
	}

	private void _testPostSiteStyleBookWithDuplicateExternalReferenceCode()
		throws Exception {

		try {
			StyleBook randomStyleBook1 = randomStyleBook();

			randomStyleBook1 = testPostSiteStyleBook_addStyleBook(
				randomStyleBook1);

			StyleBook randomStyleBook2 = randomStyleBook();

			randomStyleBook2.setExternalReferenceCode(
				randomStyleBook1.getExternalReferenceCode());

			testPostSiteStyleBook_addStyleBook(randomStyleBook2);

			Assert.fail();
		}
		catch (Problem.ProblemException problemException) {
			Problem problem = problemException.getProblem();

			Assert.assertEquals("BAD_REQUEST", problem.getStatus());
			Assert.assertEquals(
				_language.get(
					LocaleUtil.getDefault(),
					"this-external-reference-code-is-already-in-use"),
				problem.getTitle());
		}
	}

	private void _testPostSiteStyleBookWithDuplicateKey() throws Exception {
		try {
			StyleBook randomStyleBook1 = randomStyleBook();

			randomStyleBook1 = testPostSiteStyleBook_addStyleBook(
				randomStyleBook1);

			StyleBook randomStyleBook2 = randomStyleBook();

			randomStyleBook2.setKey(randomStyleBook1.getKey());

			testPostSiteStyleBook_addStyleBook(randomStyleBook2);

			Assert.fail();
		}
		catch (Problem.ProblemException problemException) {
			Assert.assertEquals(
				"A style book with the same key already exists",
				problemException.getMessage());
		}
	}

	private static final String _THEME_ID_CLASSIC = "classic_WAR_classictheme";

	private static final String _THEME_ID_DIALECT = "dialect_WAR_dialecttheme";

	@Inject
	private DepotEntryGroupRelLocalService _depotEntryGroupRelLocalService;

	@Inject
	private GroupLocalService _groupLocalService;

	@Inject
	private Language _language;

	@Inject
	private LayoutLocalService _layoutLocalService;

	@Inject
	private UserLocalService _userLocalService;

}