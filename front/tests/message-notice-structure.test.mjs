import { readFileSync, existsSync } from 'fs';
import { join } from 'path';
import { fileURLToPath } from 'url';

const root = fileURLToPath(new URL('..', import.meta.url));
const componentPath = join(root, 'src/components/MessageNoticePopover.vue');
const navbarPath = join(root, 'src/layouts/Navbar.vue');
const userLayoutPath = join(root, 'src/layouts/UserLayout.vue');

function assert(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

assert(existsSync(componentPath), 'MessageNoticePopover.vue should exist');

const component = readFileSync(componentPath, 'utf8');
const navbar = readFileSync(navbarPath, 'utf8');
const userLayout = readFileSync(userLayoutPath, 'utf8');

assert(component.includes('defineProps'), 'MessageNoticePopover should expose props');
assert(component.includes('targetPath'), 'MessageNoticePopover should accept targetPath');
assert(component.includes('getMessagePage'), 'MessageNoticePopover should load unread messages');
assert(component.includes('readMessage'), 'MessageNoticePopover should mark messages read');
assert(navbar.includes('<MessageNoticePopover target-path="/admin/message" />'), 'Navbar should render shared message popover');
assert(userLayout.includes('<MessageNoticePopover target-path="/user/message" />'), 'UserLayout should render shared message popover');
assert(!navbar.includes('getMessagePage, readMessage'), 'Navbar should not own message API calls');
assert(!userLayout.includes('getMessagePage, readMessage'), 'UserLayout should not own message API calls');

console.log('Message notice structure checks passed');
