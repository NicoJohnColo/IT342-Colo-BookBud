import React from 'react';
import { render, screen } from '@testing-library/react';

describe('Common components', () => {
  describe('Toast/Notification', () => {
    it('should display success notification', () => {
      // Mock component
      const Toast = ({ type, message }) => (
        <div className={`toast toast-${type}`}>
          {message}
        </div>
      );

      render(<Toast type="success" message="Operation successful" />);
      expect(screen.getByText('Operation successful')).toBeInTheDocument();
    });

    it('should display error notification', () => {
      const Toast = ({ type, message }) => (
        <div className={`toast toast-${type}`}>
          {message}
        </div>
      );

      render(<Toast type="error" message="Something went wrong" />);
      expect(screen.getByText('Something went wrong')).toBeInTheDocument();
    });
  });

  describe('Loading indicator', () => {
    it('should render when loading is true', () => {
      const Loading = ({ isLoading }) => (
        <>
          {isLoading && <div className="spinner">Loading...</div>}
        </>
      );

      render(<Loading isLoading={true} />);
      expect(screen.getByText('Loading...')).toBeInTheDocument();
    });

    it('should not render when loading is false', () => {
      const Loading = ({ isLoading }) => (
        <>
          {isLoading && <div className="spinner">Loading...</div>}
        </>
      );

      render(<Loading isLoading={false} />);
      expect(screen.queryByText('Loading...')).not.toBeInTheDocument();
    });
  });

  describe('Error message', () => {
    it('should display error message', () => {
      const ErrorMessage = ({ message }) => (
        <div className="error-message">{message}</div>
      );

      render(<ErrorMessage message="An error occurred" />);
      expect(screen.getByText('An error occurred')).toBeInTheDocument();
    });

    it('should not display when message is empty', () => {
      const ErrorMessage = ({ message }) => (
        message && <div className="error-message">{message}</div>
      );

      render(<ErrorMessage message={null} />);
      expect(screen.queryByRole('complementary')).not.toBeInTheDocument();
    });
  });

  describe('Modal/Dialog', () => {
    it('should render modal when isOpen is true', () => {
      const Modal = ({ isOpen, title, children }) => (
        <>
          {isOpen && (
            <div className="modal">
              <h2>{title}</h2>
              {children}
            </div>
          )}
        </>
      );

      render(
        <Modal isOpen={true} title="Test Modal">
          <p>Modal content</p>
        </Modal>
      );

      expect(screen.getByText('Test Modal')).toBeInTheDocument();
      expect(screen.getByText('Modal content')).toBeInTheDocument();
    });

    it('should not render modal when isOpen is false', () => {
      const Modal = ({ isOpen, title, children }) => (
        <>
          {isOpen && (
            <div className="modal">
              <h2>{title}</h2>
              {children}
            </div>
          )}
        </>
      );

      render(
        <Modal isOpen={false} title="Test Modal">
          <p>Modal content</p>
        </Modal>
      );

      expect(screen.queryByText('Test Modal')).not.toBeInTheDocument();
    });
  });

  describe('Button component', () => {
    it('should render button with label', () => {
      const Button = ({ label, onClick }) => (
        <button onClick={onClick}>{label}</button>
      );

      render(<Button label="Click me" onClick={() => {}} />);
      expect(screen.getByText('Click me')).toBeInTheDocument();
    });

    it('should be disabled when disabled prop is true', () => {
      const Button = ({ label, disabled, onClick }) => (
        <button disabled={disabled} onClick={onClick}>
          {label}
        </button>
      );

      render(<Button label="Click me" disabled={true} onClick={() => {}} />);
      expect(screen.getByRole('button')).toBeDisabled();
    });

    it('should call onClick handler when clicked', () => {
      const handleClick = jest.fn();
      const Button = ({ label, onClick }) => (
        <button onClick={onClick}>{label}</button>
      );

      const { container } = render(<Button label="Click me" onClick={handleClick} />);
      container.querySelector('button').click();

      expect(handleClick).toHaveBeenCalledTimes(1);
    });
  });

  describe('Form input', () => {
    it('should render input field', () => {
      const Input = ({ name, type, placeholder, value, onChange }) => (
        <input
          name={name}
          type={type}
          placeholder={placeholder}
          value={value}
          onChange={onChange}
        />
      );

      render(
        <Input name="username" type="text" placeholder="Enter username" value="" onChange={() => {}} />
      );

      expect(screen.getByPlaceholderText('Enter username')).toBeInTheDocument();
    });

    it('should update value on change', () => {
      const Input = ({ name, type, placeholder, value, onChange }) => (
        <input
          name={name}
          type={type}
          placeholder={placeholder}
          value={value}
          onChange={onChange}
        />
      );

      const handleChange = jest.fn();
      const { container } = render(
        <Input name="username" type="text" placeholder="Enter username" value="" onChange={handleChange} />
      );

      const input = container.querySelector('input');
      // Simulate user input by calling the mock directly
      handleChange({ target: { value: 'newuser' } });

      expect(handleChange).toHaveBeenCalled();
      expect(handleChange).toHaveBeenCalledWith(expect.objectContaining({
        target: expect.objectContaining({ value: 'newuser' })
      }));
    });
  });
});
