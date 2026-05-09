import { renderHook, act } from '@testing-library/react';

describe('Custom hooks', () => {
  describe('useState patterns', () => {
    it('should manage boolean state', () => {
      const { result } = renderHook(() => {
        const [isOpen, setIsOpen] = React.useState(false);
        return [isOpen, setIsOpen];
      });

      expect(result.current[0]).toBe(false);

      act(() => {
        result.current[1](true);
      });

      expect(result.current[0]).toBe(true);
    });

    it('should manage form state', () => {
      const initialForm = { username: '', email: '', password: '' };
      const { result } = renderHook(() => {
        const [form, setForm] = React.useState(initialForm);
        return [form, setForm];
      });

      expect(result.current[0]).toEqual(initialForm);

      act(() => {
        result.current[1]((prev) => ({ ...prev, username: 'testuser' }));
      });

      expect(result.current[0].username).toBe('testuser');
    });
  });

  describe('useEffect patterns', () => {
    it('should handle cleanup in useEffect', () => {
      const cleanup = jest.fn();

      const { unmount } = renderHook(() => {
        React.useEffect(() => {
          return cleanup;
        }, []);
      });

      unmount();
      expect(cleanup).toHaveBeenCalled();
    });

    it('should handle dependency array', () => {
      const sideEffect = jest.fn();

      const { rerender } = renderHook(
        ({ dep }) => {
          React.useEffect(() => {
            sideEffect();
          }, [dep]);
        },
        { initialProps: { dep: 'initial' } }
      );

      expect(sideEffect).toHaveBeenCalledTimes(1);

      rerender({ dep: 'initial' });
      expect(sideEffect).toHaveBeenCalledTimes(1);

      rerender({ dep: 'changed' });
      expect(sideEffect).toHaveBeenCalledTimes(2);
    });
  });

  describe('useCallback patterns', () => {
    it('should memoize callback function', () => {
      const { result, rerender } = renderHook(
        ({ value }) => {
          return React.useCallback(() => value, [value]);
        },
        { initialProps: { value: 'initial' } }
      );

      const firstCallback = result.current;

      rerender({ value: 'initial' });
      expect(result.current).toBe(firstCallback);

      rerender({ value: 'changed' });
      expect(result.current).not.toBe(firstCallback);
    });
  });

  describe('useReducer patterns', () => {
    it('should handle reducer with initial state', () => {
      const reducer = (state, action) => {
        switch (action.type) {
          case 'INCREMENT':
            return { count: state.count + 1 };
          case 'DECREMENT':
            return { count: state.count - 1 };
          default:
            return state;
        }
      };

      const { result } = renderHook(() =>
        React.useReducer(reducer, { count: 0 })
      );

      expect(result.current[0].count).toBe(0);

      act(() => {
        result.current[1]({ type: 'INCREMENT' });
      });

      expect(result.current[0].count).toBe(1);

      act(() => {
        result.current[1]({ type: 'DECREMENT' });
      });

      expect(result.current[0].count).toBe(0);
    });
  });
});

// Add React import for hook tests
import React from 'react';
