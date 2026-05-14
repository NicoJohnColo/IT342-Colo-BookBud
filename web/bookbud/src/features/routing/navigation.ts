import { useNavigate } from 'react-router-dom';
import { ROUTES } from './constants';

export const useAppNavigation = () => {
  const navigate = useNavigate();
  
  return {
    goHome: () => navigate(ROUTES.HOME),
    goLogin: () => navigate(ROUTES.LOGIN),
    goDashboard: () => navigate(ROUTES.DASHBOARD),
    goProfile: () => navigate(ROUTES.PROFILE),
    goAdminDashboard: () => navigate(ROUTES.ADMIN_DASHBOARD),
  };
};
