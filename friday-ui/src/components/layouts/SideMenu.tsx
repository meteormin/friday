// src/components/SideMenu.tsx
import React from 'react';
import { List, ListItemButton, ListItemText, Toolbar, IconButton, Divider } from '@mui/material';
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import { Link } from 'react-router-dom';
import MuiDrawer from '@mui/material/Drawer';
import { styled } from '@mui/material/styles';
import ToggleableProps from './ToggleableProps';

const drawerWidth: number = 240;

const Drawer = styled(MuiDrawer, { shouldForwardProp: (prop) => prop !== 'open' })(
    ({ theme, open }) => ({
        '& .MuiDrawer-paper': {
            position: 'relative',
            whiteSpace: 'nowrap',
            width: drawerWidth,
            transition: theme.transitions.create('width', {
                easing: theme.transitions.easing.sharp,
                duration: theme.transitions.duration.enteringScreen,
            }),
            boxSizing: 'border-box',
            ...(!open && {
                overflowX: 'hidden',
                transition: theme.transitions.create('width', {
                    easing: theme.transitions.easing.sharp,
                    duration: theme.transitions.duration.leavingScreen,
                }),
                width: theme.spacing(7),
                [theme.breakpoints.up('sm')]: {
                    width: theme.spacing(9),
                },
            }),
        },
    }),
);

const SideMenu: React.FC<ToggleableProps> = ({ open, toggleDrawer }) => {
    return (
        <Drawer variant="permanent" anchor="left" open={open}>
            <Toolbar
                sx={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'flex-end',
                    px: [1],
                }}
            >
                <IconButton onClick={toggleDrawer}>
                    <ChevronLeftIcon />
                </IconButton>
            </Toolbar>
            <Divider />
            <List component="nav">
                <ListItemButton component={Link} to="/">
                    <ListItemText primary="홈" />
                </ListItemButton>
                <ListItemButton component={Link} to="/about">
                    <ListItemText primary="About" />
                </ListItemButton>
                {/* 다른 메뉴 항목 추가 */}
            </List>
        </Drawer>
    );
};

export default SideMenu;
