/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {getObjectValueFromPath} from 'frontend-js-web';

import {DesignLibraryItemData, DesignLibraryResourceType} from '../types';

/**
 * Matches a listed row to the resource type that contributed it. Several types
 * may share an entry class name, so a type that declares type filters only
 * matches rows holding every value it names.
 */
export default function findResourceType(
	resourceTypes: DesignLibraryResourceType[],
	itemData?: DesignLibraryItemData
): DesignLibraryResourceType | undefined {
	if (!itemData?.entryClassName) {
		return undefined;
	}

	return resourceTypes.find((resourceType) => {
		if (resourceType.entryClassName !== itemData.entryClassName) {
			return false;
		}

		return Object.entries(resourceType.typeFilters || {}).every(
			([path, value]) =>
				String(value) ===
				String(getObjectValueFromPath({object: itemData, path}))
		);
	});
}
