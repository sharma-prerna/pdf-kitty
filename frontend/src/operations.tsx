import CompressIcon from '@mui/icons-material/Compress';
import MergeIcon from '@mui/icons-material/CallMerge';
import SplitIcon from '@mui/icons-material/CallSplit';
import ArticleIcon from '@mui/icons-material/Article';
import ImageIcon from '@mui/icons-material/Image';
import PhotoLibraryIcon from '@mui/icons-material/PhotoLibrary';
import type { SvgIconComponent } from '@mui/icons-material';

export type ControlType = 'select' | 'text' | 'number';

export interface ControlDef {
  name: string;
  label: string;
  type: ControlType;
  default: string;
  options?: { value: string; label: string }[];
  helperText?: string;
  /** Only render this control when the predicate over current values is true. */
  showIf?: (values: Record<string, string>) => boolean;
}

export interface OperationDef {
  key: string;
  endpoint: string;
  title: string;
  description: string;
  Icon: SvgIconComponent;
  color: string;
  /** react-dropzone `accept` map. */
  accept: Record<string, string[]>;
  multiple: boolean;
  reorder: boolean;
  minFiles: number;
  controls: ControlDef[];
}

const PDF = { 'application/pdf': ['.pdf'] };
const IMAGES = { 'image/jpeg': ['.jpg', '.jpeg'], 'image/png': ['.png'] };
const WORD = {
  'application/msword': ['.doc'],
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document': ['.docx'],
};

export const OPERATIONS: OperationDef[] = [
  {
    key: 'compress',
    endpoint: '/pdf/compress',
    title: 'Compress PDF',
    description: 'Reduce PDF file size with selectable quality.',
    Icon: CompressIcon,
    color: '#4f46e5',
    accept: PDF,
    multiple: false,
    reorder: false,
    minFiles: 1,
    controls: [
      {
        name: 'level',
        label: 'Compression level',
        type: 'select',
        default: 'MEDIUM',
        options: [
          { value: 'LOW', label: 'Low (best quality)' },
          { value: 'MEDIUM', label: 'Medium' },
          { value: 'HIGH', label: 'High (smallest size)' },
        ],
      },
    ],
  },
  {
    key: 'merge',
    endpoint: '/pdf/merge',
    title: 'Merge PDF',
    description: 'Combine several PDFs into one. Drag to reorder.',
    Icon: MergeIcon,
    color: '#0ea5e9',
    accept: PDF,
    multiple: true,
    reorder: true,
    minFiles: 2,
    controls: [],
  },
  {
    key: 'split',
    endpoint: '/pdf/split',
    title: 'Split PDF',
    description: 'Split by range, specific pages, or every page.',
    Icon: SplitIcon,
    color: '#9333ea',
    accept: PDF,
    multiple: false,
    reorder: false,
    minFiles: 1,
    controls: [
      {
        name: 'mode',
        label: 'Split mode',
        type: 'select',
        default: 'EVERY',
        options: [
          { value: 'EVERY', label: 'Every page' },
          { value: 'RANGE', label: 'Page range' },
          { value: 'PAGES', label: 'Specific pages' },
        ],
      },
      {
        name: 'spec',
        label: 'Pages',
        type: 'text',
        default: '',
        helperText: "e.g. '1-5' for a range, or '1,3,5' for specific pages",
        showIf: (values) => values.mode !== 'EVERY',
      },
    ],
  },
  {
    key: 'word-to-pdf',
    endpoint: '/convert/word-to-pdf',
    title: 'Word to PDF',
    description: 'Convert a Word document (.doc/.docx) into one PDF.',
    Icon: ArticleIcon,
    color: '#0891b2',
    accept: WORD,
    multiple: false,
    reorder: false,
    minFiles: 1,
    controls: [],
  },
  {
    key: 'image-to-pdf',
    endpoint: '/convert/image-to-pdf',
    title: 'Image to PDF',
    description: 'Combine JPG/PNG images into one PDF. Drag to reorder.',
    Icon: ImageIcon,
    color: '#16a34a',
    accept: IMAGES,
    multiple: true,
    reorder: true,
    minFiles: 1,
    controls: [],
  },
  {
    key: 'pdf-to-image',
    endpoint: '/convert/pdf-to-image',
    title: 'PDF to Image',
    description: 'Render PDF pages to JPG or PNG at a chosen DPI.',
    Icon: PhotoLibraryIcon,
    color: '#ea580c',
    accept: PDF,
    multiple: false,
    reorder: false,
    minFiles: 1,
    controls: [
      {
        name: 'format',
        label: 'Image format',
        type: 'select',
        default: 'PNG',
        options: [
          { value: 'PNG', label: 'PNG' },
          { value: 'JPG', label: 'JPG' },
        ],
      },
      {
        name: 'dpi',
        label: 'DPI',
        type: 'number',
        default: '150',
        helperText: 'Higher DPI = sharper, larger images (36–600)',
      },
    ],
  },
];

export const operationByKey = (key: string): OperationDef | undefined =>
  OPERATIONS.find((op) => op.key === key);
