import { describe, expect, it } from 'vitest';
import { screen } from '@testing-library/react';
import { renderWithProviders } from '../test/utils';
import Dashboard from './Dashboard';
import { OPERATIONS } from '../operations';

describe('Dashboard', () => {
  it('renders a card for every operation', () => {
    renderWithProviders(<Dashboard />);
    OPERATIONS.forEach((op) => {
      expect(screen.getByText(op.title)).toBeInTheDocument();
    });
  });
});
