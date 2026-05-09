describe('Validation utilities', () => {
  describe('Email validation', () => {
    it('should validate correct email formats', () => {
      const validEmails = [
        'test@example.com',
        'user.name@example.co.uk',
        'user+tag@example.com',
      ];

      validEmails.forEach((email) => {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        expect(emailRegex.test(email)).toBe(true);
      });
    });

    it('should reject invalid email formats', () => {
      const invalidEmails = ['plainaddress', '@example.com', 'user@', 'user name@example.com'];

      invalidEmails.forEach((email) => {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        expect(emailRegex.test(email)).toBe(false);
      });
    });
  });

  describe('Password validation', () => {
    it('should validate strong passwords', () => {
      const strongPasswords = ['Pass@123', 'MyP@ssw0rd', 'Str0ng!Pass'];

      strongPasswords.forEach((password) => {
        // Check minimum 8 characters, at least one uppercase, one lowercase, one number
        const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/;
        expect(passwordRegex.test(password)).toBe(true);
      });
    });

    it('should reject weak passwords', () => {
      const weakPasswords = ['password', '12345678', 'Pass', 'pass123'];

      weakPasswords.forEach((password) => {
        const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/;
        expect(passwordRegex.test(password)).toBe(false);
      });
    });
  });

  describe('Username validation', () => {
    it('should validate correct usernames', () => {
      const validUsernames = ['user123', 'john_doe', 'jane.smith', 'test_user_123'];

      validUsernames.forEach((username) => {
        const usernameRegex = /^[a-zA-Z0-9._-]{3,20}$/;
        expect(usernameRegex.test(username)).toBe(true);
      });
    });

    it('should reject invalid usernames', () => {
      const invalidUsernames = ['ab', 'user@name', 'user name', 'a'.repeat(21)];

      invalidUsernames.forEach((username) => {
        const usernameRegex = /^[a-zA-Z0-9._-]{3,20}$/;
        expect(usernameRegex.test(username)).toBe(false);
      });
    });
  });
});
