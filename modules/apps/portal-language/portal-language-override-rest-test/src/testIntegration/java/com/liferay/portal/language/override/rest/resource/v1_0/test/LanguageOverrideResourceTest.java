/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.language.override.rest.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.language.override.rest.client.dto.v1_0.Creator;
import com.liferay.portal.language.override.rest.client.dto.v1_0.LanguageOverride;
import com.liferay.portal.language.override.rest.client.pagination.Page;
import com.liferay.portal.language.override.rest.client.pagination.Pagination;
import com.liferay.portal.language.override.service.PLOEntryLocalService;
import com.liferay.portal.test.rule.Inject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Thiago Buarque
 */
@RunWith(Arquillian.class)
public class LanguageOverrideResourceTest
	extends BaseLanguageOverrideResourceTestCase {

	@Override
	@Test
	public void testDeleteLanguageOverride() throws Exception {
		LanguageOverride languageOverride =
			languageOverrideResource.postLanguageOverride(
				randomLanguageOverride());

		languageOverrideResource.deleteLanguageOverride(
			languageOverride.getKey(), languageOverride.getLanguageId());

		Page<LanguageOverride> page =
			languageOverrideResource.getLanguageOverridesPage(
				languageOverride.getKey(), Pagination.of(1, 10), null);

		Collection<LanguageOverride> languageOverrides = page.getItems();

		Assert.assertEquals(
			languageOverrides.toString(), 0, languageOverrides.size());
	}

	@Override
	@Test
	public void testEscapeRegexInStringFields() throws Exception {

		// The languageId must be a valid locale, so the generated test that
		// sets every string field to a regex does not apply.

	}

	@Override
	@Test
	public void testGetLanguageOverridesPage() throws Exception {
		Page<LanguageOverride> page =
			languageOverrideResource.getLanguageOverridesPage(
				null, Pagination.of(1, 1), null);

		long totalCount = page.getTotalCount();

		String prefix = RandomTestUtil.randomString();

		languageOverrideResource.postLanguageOverride(
			_languageOverride(prefix + "1", "alpha"));
		languageOverrideResource.postLanguageOverride(
			_languageOverride(prefix + "2", "beta"));

		page = languageOverrideResource.getLanguageOverridesPage(
			null, Pagination.of(1, 1), null);

		Assert.assertEquals(totalCount + 2, page.getTotalCount());

		Page<LanguageOverride> prefixPage =
			languageOverrideResource.getLanguageOverridesPage(
				prefix, Pagination.of(1, 10), null);

		Collection<LanguageOverride> languageOverrides = prefixPage.getItems();

		Assert.assertEquals(
			languageOverrides.toString(), 2, languageOverrides.size());
	}

	@Test
	public void testGetLanguageOverridesPageReturnsCreator() throws Exception {
		LanguageOverride languageOverride =
			languageOverrideResource.postLanguageOverride(
				randomLanguageOverride());

		Page<LanguageOverride> page =
			languageOverrideResource.getLanguageOverridesPage(
				languageOverride.getKey(), Pagination.of(1, 10), null);

		LanguageOverride persistedLanguageOverride = _getLanguageOverride(
			page, languageOverride.getKey());

		Creator creator = persistedLanguageOverride.getCreator();

		User user = UserTestUtil.getAdminUser(testCompany.getCompanyId());

		Assert.assertEquals(Long.valueOf(user.getUserId()), creator.getId());
	}

	@Test
	public void testGetLanguageOverridesPageReturnsOverridesOnly()
		throws Exception {

		languageOverrideResource.postLanguageOverride(randomLanguageOverride());

		Page<LanguageOverride> page =
			languageOverrideResource.getLanguageOverridesPage(
				null, Pagination.of(1, 1), null);

		// The endpoint returns override rows only, never the merged dictionary,
		// so its total matches the actual PLOEntry count for the company.

		Assert.assertEquals(
			_ploEntryLocalService.getPLOEntriesCount(
				testCompany.getCompanyId()),
			page.getTotalCount());
	}

	@Test
	public void testGetLanguageOverridesPageSearch() throws Exception {
		String keywords = RandomTestUtil.randomString();

		languageOverrideResource.postLanguageOverride(
			_languageOverride("key-" + keywords, "alpha"));
		languageOverrideResource.postLanguageOverride(
			_languageOverride("other", "value-" + keywords));

		languageOverrideResource.postLanguageOverride(
			_languageOverride("unrelated", "beta"));

		Page<LanguageOverride> page =
			languageOverrideResource.getLanguageOverridesPage(
				keywords, Pagination.of(1, 10), null);

		Collection<LanguageOverride> languageOverrides = page.getItems();

		// One match on the key, one match on the value

		Assert.assertEquals(
			languageOverrides.toString(), 2, languageOverrides.size());
	}

	@Test
	public void testGetLanguageOverridesPageSortByKey() throws Exception {
		String prefix = RandomTestUtil.randomString();

		languageOverrideResource.postLanguageOverride(
			_languageOverride(prefix + "c", "gamma"));
		languageOverrideResource.postLanguageOverride(
			_languageOverride(prefix + "a", "alpha"));
		languageOverrideResource.postLanguageOverride(
			_languageOverride(prefix + "b", "beta"));

		Page<LanguageOverride> page =
			languageOverrideResource.getLanguageOverridesPage(
				prefix, Pagination.of(1, 2), "key:asc");

		List<LanguageOverride> languageOverrides = new ArrayList<>(
			page.getItems());

		LanguageOverride firstLanguageOverride = languageOverrides.get(0);
		LanguageOverride secondLanguageOverride = languageOverrides.get(1);

		Assert.assertEquals(prefix + "a", firstLanguageOverride.getKey());
		Assert.assertEquals(prefix + "b", secondLanguageOverride.getKey());
		Assert.assertEquals(3, page.getTotalCount());
	}

	@Override
	@Test
	public void testGetLanguageOverridesPageWithSortDateTime()
		throws Exception {

		// The resource is not an EntityModelResource; sorting is covered by
		// testGetLanguageOverridesPageSortByKey.

	}

	@Override
	@Test
	public void testGetLanguageOverridesPageWithSortDouble() throws Exception {

		// The resource is not an EntityModelResource; sorting is covered by
		// testGetLanguageOverridesPageSortByKey.

	}

	@Override
	@Test
	public void testGetLanguageOverridesPageWithSortInteger() throws Exception {

		// The resource is not an EntityModelResource; sorting is covered by
		// testGetLanguageOverridesPageSortByKey.

	}

	@Override
	@Test
	public void testGetLanguageOverridesPageWithSortString() throws Exception {

		// The resource is not an EntityModelResource; sorting is covered by
		// testGetLanguageOverridesPageSortByKey.

	}

	@Test
	public void testPostLanguageOverridePreservesAuditDates() throws Exception {
		Date dateCreated = new Date(1000000000000L);
		Date dateModified = new Date(1100000000000L);

		LanguageOverride languageOverride = randomLanguageOverride();

		languageOverride.setDateCreated(dateCreated);
		languageOverride.setDateModified(dateModified);

		languageOverrideResource.postLanguageOverride(languageOverride);

		Page<LanguageOverride> page =
			languageOverrideResource.getLanguageOverridesPage(
				languageOverride.getKey(), Pagination.of(1, 10), null);

		LanguageOverride persistedLanguageOverride = _getLanguageOverride(
			page, languageOverride.getKey());

		Assert.assertEquals(
			dateCreated, persistedLanguageOverride.getDateCreated());
		Assert.assertEquals(
			dateModified, persistedLanguageOverride.getDateModified());
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {"key", "languageId", "value"};
	}

	@Override
	protected LanguageOverride randomLanguageOverride() throws Exception {
		LanguageOverride languageOverride = super.randomLanguageOverride();

		languageOverride.setLanguageId("en_US");

		return languageOverride;
	}

	@Override
	protected LanguageOverride testGetLanguageOverridesPage_addLanguageOverride(
			LanguageOverride languageOverride)
		throws Exception {

		return languageOverrideResource.postLanguageOverride(languageOverride);
	}

	@Override
	protected LanguageOverride testPostLanguageOverride_addLanguageOverride(
			LanguageOverride languageOverride)
		throws Exception {

		return languageOverrideResource.postLanguageOverride(languageOverride);
	}

	private LanguageOverride _getLanguageOverride(
		Page<LanguageOverride> page, String key) {

		for (LanguageOverride languageOverride : page.getItems()) {
			if (key.equals(languageOverride.getKey())) {
				return languageOverride;
			}
		}

		return null;
	}

	private LanguageOverride _languageOverride(String key, String value) {
		LanguageOverride languageOverride = new LanguageOverride();

		languageOverride.setKey(key);
		languageOverride.setLanguageId("en_US");
		languageOverride.setValue(value);

		return languageOverride;
	}

	@Inject
	private PLOEntryLocalService _ploEntryLocalService;

}