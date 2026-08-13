/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {openModal} from 'frontend-js-components-web';
import {loadModule} from 'frontend-js-web';
import React from 'react';

import {DesignLibraryResourceCreationItem} from './types';

type CreationModalComponent = React.ComponentType<
	Record<string, unknown> & {closeModal: () => void}
>;

export default async function openCreationModal({
	module,
	moduleProps,
}: DesignLibraryResourceCreationItem) {
	const Component: CreationModalComponent = await loadModule(module);

	openModal({
		contentComponent: ({closeModal}: {closeModal: () => void}) => (
			<Component {...moduleProps} closeModal={closeModal} />
		),
	});
}
