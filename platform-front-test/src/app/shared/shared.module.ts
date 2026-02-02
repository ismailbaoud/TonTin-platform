import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

// Directives
import { HighlightDirective } from './directives/highlight.directive';
import { ClickOutsideDirective } from './directives/click-outside.directive';
import { LazyLoadImageDirective } from './directives/lazy-load-image.directive';
import { PermissionDirective } from './directives/permission.directive';

// Pipes
import { SafeHtmlPipe } from './pipes/safe-html.pipe';
import { TruncatePipe } from './pipes/truncate.pipe';
import { TimeAgoPipe } from './pipes/time-ago.pipe';
import { FilterPipe } from './pipes/filter.pipe';
import { SortPipe } from './pipes/sort.pipe';

// Components
import { LoaderComponent } from './components/loader/loader.component';
import { ModalComponent } from './components/modal/modal.component';
import { ConfirmDialogComponent } from './components/confirm-dialog/confirm-dialog.component';
import { ToastComponent } from './components/toast/toast.component';
import { PaginationComponent } from './components/pagination/pagination.component';
import { DataTableComponent } from './components/data-table/data-table.component';
import { SearchBarComponent } from './components/search-bar/search-bar.component';
import { BreadcrumbComponent } from './components/breadcrumb/breadcrumb.component';
import { CardComponent } from './components/card/card.component';
import { ButtonComponent } from './components/button/button.component';
import { InputComponent } from './components/input/input.component';
import { SelectComponent } from './components/select/select.component';
import { DatePickerComponent } from './components/date-picker/date-picker.component';

/**
 * Shared Module
 *
 * This module contains reusable components, directives, and pipes
 * that can be used across multiple feature modules.
 *
 * Key Features:
 * - Reusable UI components
 * - Custom directives
 * - Custom pipes
 * - Can be imported by any feature module
 * - Exports common Angular modules (CommonModule, FormsModule, etc.)
 */
@NgModule({
  declarations: [
    // Directives
    HighlightDirective,
    ClickOutsideDirective,
    LazyLoadImageDirective,
    PermissionDirective,

    // Pipes
    SafeHtmlPipe,
    TruncatePipe,
    TimeAgoPipe,
    FilterPipe,
    SortPipe,

    // Components
    LoaderComponent,
    ModalComponent,
    ConfirmDialogComponent,
    ToastComponent,
    PaginationComponent,
    DataTableComponent,
    SearchBarComponent,
    BreadcrumbComponent,
    CardComponent,
    ButtonComponent,
    InputComponent,
    SelectComponent,
    DatePickerComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule
  ],
  exports: [
    // Angular modules
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,

    // Directives
    HighlightDirective,
    ClickOutsideDirective,
    LazyLoadImageDirective,
    PermissionDirective,

    // Pipes
    SafeHtmlPipe,
    TruncatePipe,
    TimeAgoPipe,
    FilterPipe,
    SortPipe,

    // Components
    LoaderComponent,
    ModalComponent,
    ConfirmDialogComponent,
    ToastComponent,
    PaginationComponent,
    DataTableComponent,
    SearchBarComponent,
    BreadcrumbComponent,
    CardComponent,
    ButtonComponent,
    InputComponent,
    SelectComponent,
    DatePickerComponent
  ]
})
export class SharedModule { }
