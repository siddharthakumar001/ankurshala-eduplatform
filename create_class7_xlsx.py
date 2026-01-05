"""
Convert Class 7 Curriculum CSV to XLSX format for Ankurshala import
This script creates a properly formatted XLSX file with the curriculum data
"""

import pandas as pd
import openpyxl
from openpyxl.styles import Font, PatternFill, Alignment
from pathlib import Path

def create_curriculum_xlsx():
    # Read the CSV file
    csv_path = Path(__file__).parent / 'class-7-curriculum.csv'
    df = pd.read_csv(csv_path)
    
    # Output XLSX path
    xlsx_path = Path(__file__).parent / 'Class_7_Curriculum.xlsx'
    
    # Create Excel writer
    with pd.ExcelWriter(xlsx_path, engine='openpyxl') as writer:
        # Write dataframe to Excel
        df.to_excel(writer, sheet_name='Curriculum', index=False)
        
        # Get the workbook and worksheet
        workbook = writer.book
        worksheet = writer.sheets['Curriculum']
        
        # Style the header row
        header_fill = PatternFill(start_color='4472C4', end_color='4472C4', fill_type='solid')
        header_font = Font(bold=True, color='FFFFFF', size=11)
        
        for cell in worksheet[1]:
            cell.fill = header_fill
            cell.font = header_font
            cell.alignment = Alignment(horizontal='center', vertical='center', wrap_text=True)
        
        # Set column widths
        column_widths = {
            'A': 15,  # Board
            'B': 8,   # Grade
            'C': 20,  # Subject
            'D': 35,  # Chapter
            'E': 60,  # Topics
            'F': 50   # Related Topics
        }
        
        for col, width in column_widths.items():
            worksheet.column_dimensions[col].width = width
        
        # Format data rows
        for row in worksheet.iter_rows(min_row=2, max_row=worksheet.max_row):
            for cell in row:
                cell.alignment = Alignment(vertical='top', wrap_text=True)
        
        # Set row height for header
        worksheet.row_dimensions[1].height = 30
        
        # Set auto height for data rows
        for row in range(2, worksheet.max_row + 1):
            worksheet.row_dimensions[row].height = 40
    
    print(f"✅ Successfully created: {xlsx_path}")
    print(f"\n📊 Summary:")
    print(f"   - Total rows: {len(df)}")
    print(f"   - Boards: {', '.join(df['Board'].unique())}")
    print(f"   - Subjects: {', '.join(df['Subject'].unique())}")
    print(f"\n🚀 Next steps:")
    print(f"   1. Review the file: {xlsx_path.name}")
    print(f"   2. Upload via: POST /admin/content/import/curriculum")
    print(f"   3. Use dryRun=true first to validate")

if __name__ == '__main__':
    create_curriculum_xlsx()
