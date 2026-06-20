import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import FileList from './FileList';

const makeFile = (name: string) => new File(['data'], name, { type: 'application/pdf' });

describe('FileList', () => {
  it('renders files and removes by index', async () => {
    const onRemove = vi.fn();
    render(
      <FileList
        files={[makeFile('a.pdf'), makeFile('b.pdf')]}
        reorder
        onRemove={onRemove}
        onMove={vi.fn()}
      />,
    );

    expect(screen.getByText('a.pdf')).toBeInTheDocument();
    expect(screen.getByText('b.pdf')).toBeInTheDocument();

    await userEvent.click(screen.getByLabelText('remove-0'));
    expect(onRemove).toHaveBeenCalledWith(0);
  });

  it('moves a file down', async () => {
    const onMove = vi.fn();
    render(
      <FileList files={[makeFile('a.pdf'), makeFile('b.pdf')]} reorder onRemove={vi.fn()} onMove={onMove} />,
    );
    await userEvent.click(screen.getByLabelText('move-down-0'));
    expect(onMove).toHaveBeenCalledWith(0, 1);
  });

  it('disables move-up on the first item', () => {
    render(
      <FileList files={[makeFile('a.pdf'), makeFile('b.pdf')]} reorder onRemove={vi.fn()} onMove={vi.fn()} />,
    );
    expect(screen.getByLabelText('move-up-0')).toBeDisabled();
  });
});
