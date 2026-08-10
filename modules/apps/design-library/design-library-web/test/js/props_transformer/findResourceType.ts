/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import findResourceType from '../../../src/main/resources/META-INF/resources/js/props_transformer/findResourceType';
import {DesignLibraryResourceType} from '../../../src/main/resources/META-INF/resources/js/types';

const LAYOUT_PAGE_TEMPLATE_ENTRY =
	'com.liferay.layout.page.template.model.LayoutPageTemplateEntry';

const TEMPLATE_ENTRY = 'com.liferay.template.model.TemplateEntry';

const STYLE_BOOK: DesignLibraryResourceType = {
	color: '--purple',
	defaultActionId: 'edit',
	entryClassName: 'com.liferay.style.book.model.StyleBookEntry',
	key: 'style-book',
	label: 'Style Book',
	symbol: 'book',
};

const MASTER_PAGE: DesignLibraryResourceType = {
	color: '--blue',
	defaultActionId: 'edit',
	entryClassName: LAYOUT_PAGE_TEMPLATE_ENTRY,
	key: 'master-page',
	label: 'Master Page',
	symbol: 'page',
	typeFilters: {'embedded.type': '3'},
};

const WEB_CONTENT_TEMPLATE: DesignLibraryResourceType = {
	color: '--teal',
	defaultActionId: 'edit',
	entryClassName: TEMPLATE_ENTRY,
	key: 'web-content-template',
	label: 'Web Content Template',
	symbol: 'web-content',
	typeFilters: {
		'embedded.infoItemClassName':
			'com.liferay.journal.model.JournalArticle',
		'embedded.infoItemFormVariationKey': '38217',
	},
};

describe('findResourceType', () => {
	it('matches a type that is identified by its entry class name alone', () => {
		expect(
			findResourceType([STYLE_BOOK, MASTER_PAGE], {
				entryClassName: STYLE_BOOK.entryClassName,
			})
		).toBe(STYLE_BOOK);
	});

	it('matches a type that shares an entry class name by its discriminator', () => {
		expect(
			findResourceType([STYLE_BOOK, MASTER_PAGE], {
				embedded: {type: '3'},
				entryClassName: LAYOUT_PAGE_TEMPLATE_ENTRY,
			})
		).toBe(MASTER_PAGE);
	});

	it('does not claim a sibling row of the same entry class name', () => {
		expect(
			findResourceType([STYLE_BOOK, MASTER_PAGE], {
				embedded: {type: '1'},
				entryClassName: LAYOUT_PAGE_TEMPLATE_ENTRY,
			})
		).toBeUndefined();
	});

	it('does not claim a row whose discriminator is missing', () => {
		expect(
			findResourceType([STYLE_BOOK, MASTER_PAGE], {
				entryClassName: LAYOUT_PAGE_TEMPLATE_ENTRY,
			})
		).toBeUndefined();
	});

	it('compares discriminators as strings', () => {
		expect(
			findResourceType([MASTER_PAGE], {
				embedded: {type: 3},
				entryClassName: LAYOUT_PAGE_TEMPLATE_ENTRY,
			})
		).toBe(MASTER_PAGE);
	});

	it('matches a type identified by more than one field', () => {
		expect(
			findResourceType([WEB_CONTENT_TEMPLATE], {
				embedded: {
					infoItemClassName:
						'com.liferay.journal.model.JournalArticle',
					infoItemFormVariationKey: '38217',
				},
				entryClassName: TEMPLATE_ENTRY,
			})
		).toBe(WEB_CONTENT_TEMPLATE);
	});

	it('does not claim a row matching only some of the fields', () => {
		expect(
			findResourceType([WEB_CONTENT_TEMPLATE], {
				embedded: {
					infoItemClassName:
						'com.liferay.journal.model.JournalArticle',
					infoItemFormVariationKey: '99999',
				},
				entryClassName: TEMPLATE_ENTRY,
			})
		).toBeUndefined();
	});

	it('returns nothing for a row with no entry class name', () => {
		expect(findResourceType([STYLE_BOOK], {})).toBeUndefined();
	});
});
