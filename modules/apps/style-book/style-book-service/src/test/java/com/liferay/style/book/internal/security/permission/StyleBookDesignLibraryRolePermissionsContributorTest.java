/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.style.book.internal.security.permission;

import com.liferay.depot.constants.DepotRolesConstants;
import com.liferay.depot.role.contributor.DesignLibraryRolePermission;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.style.book.constants.StyleBookActionKeys;
import com.liferay.style.book.constants.StyleBookConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Thiago Buarque
 */
public class StyleBookDesignLibraryRolePermissionsContributorTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testGetDesignLibraryRolePermissions() {
		StyleBookDesignLibraryRolePermissionsContributor
			styleBookDesignLibraryRolePermissionsContributor =
				new StyleBookDesignLibraryRolePermissionsContributor();

		List<DesignLibraryRolePermission> designLibraryRolePermissions =
			styleBookDesignLibraryRolePermissionsContributor.
				getDesignLibraryRolePermissions();

		Assert.assertEquals(
			designLibraryRolePermissions.toString(), 3,
			designLibraryRolePermissions.size());

		Map<String, DesignLibraryRolePermission>
			designLibraryRolePermissionsMap = new HashMap<>();

		for (DesignLibraryRolePermission designLibraryRolePermission :
				designLibraryRolePermissions) {

			designLibraryRolePermissionsMap.put(
				designLibraryRolePermission.getRoleName(),
				designLibraryRolePermission);
		}

		for (String roleName :
				List.of(
					DepotRolesConstants.DESIGN_LIBRARY_ADMINISTRATOR,
					DepotRolesConstants.DESIGN_LIBRARY_CONTENT_REVIEWER,
					DepotRolesConstants.DESIGN_LIBRARY_OWNER)) {

			DesignLibraryRolePermission designLibraryRolePermission =
				designLibraryRolePermissionsMap.get(roleName);

			Assert.assertNotNull(roleName, designLibraryRolePermission);
			Assert.assertEquals(
				StyleBookConstants.RESOURCE_NAME,
				designLibraryRolePermission.getResourceName());
			Assert.assertArrayEquals(
				new String[] {StyleBookActionKeys.MANAGE_STYLE_BOOK_ENTRIES},
				designLibraryRolePermission.getActionKeys());
		}
	}

}