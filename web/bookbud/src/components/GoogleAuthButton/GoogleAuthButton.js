import React, { useEffect, useRef } from 'react';

const GOOGLE_CLIENT_ID = '219267074951-l7rubl8hm9v4edpfcakd0sbrhl485sag.apps.googleusercontent.com';
const GOOGLE_SCRIPT_ID = 'google-identity-services';

const GoogleAuthButton = ({ onSuccess, text = 'signin_with', label = 'Continue with Google' }) => {
  const containerRef = useRef(null);
  const onSuccessRef = useRef(onSuccess);

  useEffect(() => {
    onSuccessRef.current = onSuccess;
  }, [onSuccess]);

  useEffect(() => {
    const renderGoogleButton = () => {
      if (!window.google?.accounts?.id || !containerRef.current) {
        return;
      }

      window.google.accounts.id.initialize({
        client_id: GOOGLE_CLIENT_ID,
        callback: (response) => {
          if (response?.credential) {
            onSuccessRef.current(response.credential);
          }
        },
      });

      containerRef.current.innerHTML = '';
      window.google.accounts.id.renderButton(containerRef.current, {
        theme: 'outline',
        size: 'large',
        shape: 'pill',
        width: '100%',
        text,
      });
    };

    if (window.google?.accounts?.id) {
      renderGoogleButton();
      return undefined;
    }

    let script = document.getElementById(GOOGLE_SCRIPT_ID);
    if (!script) {
      script = document.createElement('script');
      script.id = GOOGLE_SCRIPT_ID;
      script.src = 'https://accounts.google.com/gsi/client';
      script.async = true;
      script.defer = true;
      document.head.appendChild(script);
    }

    script.addEventListener('load', renderGoogleButton);
    return () => {
      script.removeEventListener('load', renderGoogleButton);
    };
  }, [text]);

  return (
    <div className="google-auth-wrap">
      <div ref={containerRef} aria-label={label} />
    </div>
  );
};

export default GoogleAuthButton;